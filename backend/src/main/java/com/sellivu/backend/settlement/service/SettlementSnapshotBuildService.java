package com.sellivu.backend.settlement.service;

import com.sellivu.backend.settlement.domain.MatchStatus;
import com.sellivu.backend.settlement.repository.SettlementFeeRawRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderRawRepository;
import com.sellivu.backend.settlement.repository.SettlementOrderSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementSnapshotBuildService {

    private static final long ISSUE_ORDER_ONLY = 1L << 0;
    private static final long ISSUE_FEE_ONLY = 1L << 1;
    private static final long ISSUE_SETTLEMENT_MISMATCH = 1L << 2;
    private static final long ISSUE_COMMISSION_MISMATCH = 1L << 3;
    private static final long ISSUE_NET_MISMATCH = 1L << 4;
    private static final long ISSUE_REFUND_CANDIDATE = 1L << 5;
    private static final long ISSUE_NEEDS_USER_INPUT = 1L << 6;

    private final SettlementOrderRawRepository settlementOrderRawRepository;
    private final SettlementFeeRawRepository settlementFeeRawRepository;
    private final SettlementOrderSnapshotRepository settlementOrderSnapshotRepository;
    private final JdbcTemplate jdbcTemplate;

    public int build(Long runId) {
        long totalStartedAt = System.currentTimeMillis();

        long deleteStartedAt = System.currentTimeMillis();
        settlementOrderSnapshotRepository.deleteAllByRunId(runId);
        log.info("[PERF] snapshot delete old runId={} took={}ms",
                runId, System.currentTimeMillis() - deleteStartedAt);

        long loadStartedAt = System.currentTimeMillis();
        long orderRows = settlementOrderRawRepository.countByRunId(runId);
        long feeRows = settlementFeeRawRepository.countByRunId(runId);
        log.info("[PERF] snapshot raw load runId={} orderRows={} feeRows={} took={}ms",
                runId, orderRows, feeRows, System.currentTimeMillis() - loadStartedAt);

        long buildStartedAt = System.currentTimeMillis();
        int inserted = jdbcTemplate.update(SNAPSHOT_INSERT_SQL, ps -> {
            ps.setLong(1, runId);
            ps.setLong(2, runId);
            ps.setLong(3, runId);
            ps.setLong(4, ISSUE_ORDER_ONLY);
            ps.setLong(5, ISSUE_FEE_ONLY);
            ps.setLong(6, ISSUE_SETTLEMENT_MISMATCH);
            ps.setLong(7, ISSUE_COMMISSION_MISMATCH);
            ps.setLong(8, ISSUE_NET_MISMATCH);
            ps.setLong(9, ISSUE_REFUND_CANDIDATE);
            ps.setLong(10, ISSUE_NEEDS_USER_INPUT);
        });
        log.info("[PERF] snapshot grouping runId={} joinKeys={} took={}ms source=insertedRows",
                runId, inserted, 0);
        log.info("[PERF] snapshot sql build runId={} snapshots={} took={}ms",
                runId, inserted, System.currentTimeMillis() - buildStartedAt);

        long saveStartedAt = System.currentTimeMillis();
        log.info("[PERF] snapshot batch insert runId={} snapshots={} took={}ms",
                runId, inserted, System.currentTimeMillis() - saveStartedAt);

        log.info("[PERF] snapshot total runId={} total={}ms",
                runId, System.currentTimeMillis() - totalStartedAt);

        return inserted;
    }
    private static final String SNAPSHOT_INSERT_SQL = """
            with order_agg as (
                select
                    join_key,
                    min(id) as order_row_id,
                    min(upload_id) as order_upload_id,
                    min(order_no) as order_no,
                    min(product_order_no) as product_order_no,
                    min(product_name) as product_name,
                    min(payment_date) as paid_at,
                    min(settlement_completed_date) as settlement_date,
                    sum(coalesce(settlement_expected_amount, 0)) as order_settlement_amount,
                    sum(
                        coalesce(npay_fee_amount, 0)
                        + coalesce(sales_linked_fee_total, 0)
                        + coalesce(installment_fee_amount, 0)
                        - coalesce(benefit_amount, 0)
                    ) as order_commission_amount
                from settlement_order_raw
                where run_id = ?
                group by join_key
            ),
            fee_agg as (
                select
                    join_key,
                    min(id) as fee_row_id,
                    min(upload_id) as fee_upload_id,
                    min(order_no) as order_no,
                    min(product_order_no) as product_order_no,
                    min(product_name) as product_name,
                    min(settlement_completed_date) as settlement_date,
                    sum(coalesce(fee_base_amount, 0)) as fee_settlement_amount,
                    sum(coalesce(commission_amount, 0)) as fee_commission_amount
                from settlement_fee_raw
                where run_id = ?
                group by join_key
            ),
            joined as (
                select
                    coalesce(o.join_key, f.join_key) as join_key,
                    o.order_row_id,
                    f.fee_row_id,
                    o.order_upload_id,
                    f.fee_upload_id,
                    coalesce(o.order_no, f.order_no) as order_no,
                    coalesce(o.product_order_no, f.product_order_no) as product_order_no,
                    coalesce(o.product_name, f.product_name) as product_name,
                    o.paid_at,
                    coalesce(o.settlement_date, f.settlement_date) as settlement_date,
                    coalesce(o.order_settlement_amount, 0) as order_settlement_amount,
                    coalesce(o.order_commission_amount, 0) as order_commission_amount,
                    coalesce(f.fee_settlement_amount, 0) as fee_settlement_amount,
                    coalesce(f.fee_commission_amount, 0) as fee_commission_amount,
                    case
                        when o.join_key is not null and f.join_key is not null then 'MATCHED'
                        when o.join_key is not null then 'ORDER_ONLY'
                        else 'FEE_ONLY'
                    end as match_status
                from order_agg o
                full outer join fee_agg f on f.join_key = o.join_key
            )
            insert into settlement_order_snapshot (
                run_id,
                join_key,
                order_no,
                product_order_no,
                match_status,
                order_row_id,
                fee_row_id,
                order_upload_id,
                fee_upload_id,
                product_name,
                paid_at,
                settlement_date,
                order_settlement_amount,
                order_commission_amount,
                order_net_amount,
                fee_settlement_amount,
                fee_commission_amount,
                fee_net_amount,
                resolved_settlement_amount,
                resolved_commission_amount,
                resolved_net_amount,
                settlement_amount_matched,
                commission_amount_matched,
                net_amount_matched,
                has_issue,
                issue_count,
                issue_mask,
                primary_issue_code,
                refund_candidate,
                needs_user_input,
                last_aggregated_at
            )
            select
                ? as run_id,
                join_key,
                order_no,
                product_order_no,
                match_status,
                order_row_id,
                fee_row_id,
                order_upload_id,
                fee_upload_id,
                product_name,
                paid_at,
                settlement_date,
                order_settlement_amount,
                order_commission_amount,
                order_settlement_amount - order_commission_amount as order_net_amount,
                fee_settlement_amount,
                fee_commission_amount,
                fee_settlement_amount - fee_commission_amount as fee_net_amount,
                order_settlement_amount as resolved_settlement_amount,
                order_commission_amount as resolved_commission_amount,
                order_settlement_amount - order_commission_amount as resolved_net_amount,
                order_settlement_amount = fee_settlement_amount as settlement_amount_matched,
                order_commission_amount = fee_commission_amount as commission_amount_matched,
                (order_settlement_amount - order_commission_amount) = (fee_settlement_amount - fee_commission_amount) as net_amount_matched,
                (
                    (case when match_status = 'ORDER_ONLY' then 1 else 0 end) +
                    (case when match_status = 'FEE_ONLY' then 1 else 0 end) +
                    (case when order_settlement_amount <> fee_settlement_amount then 1 else 0 end) +
                    (case when order_commission_amount <> fee_commission_amount then 1 else 0 end) +
                    (case when (order_settlement_amount - order_commission_amount) <> (fee_settlement_amount - fee_commission_amount) then 1 else 0 end)
                ) > 0 as has_issue,
                (case when match_status = 'ORDER_ONLY' then 1 else 0 end) +
                (case when match_status = 'FEE_ONLY' then 1 else 0 end) +
                (case when order_settlement_amount <> fee_settlement_amount then 1 else 0 end) +
                (case when order_commission_amount <> fee_commission_amount then 1 else 0 end) +
                (case when (order_settlement_amount - order_commission_amount) <> (fee_settlement_amount - fee_commission_amount) then 1 else 0 end) as issue_count,
                (
                    (case when match_status = 'ORDER_ONLY' then ? else 0 end) |
                    (case when match_status = 'FEE_ONLY' then ? else 0 end) |
                    (case when order_settlement_amount <> fee_settlement_amount then ? else 0 end) |
                    (case when order_commission_amount <> fee_commission_amount then ? else 0 end) |
                    (case when (order_settlement_amount - order_commission_amount) <> (fee_settlement_amount - fee_commission_amount) then ? else 0 end) |
                    (case when order_commission_amount - fee_commission_amount < 0 then ? else 0 end) |
                    (case when match_status in ('ORDER_ONLY', 'FEE_ONLY') then ? else 0 end)
                ) as issue_mask,
                case
                    when match_status = 'ORDER_ONLY' then 'ORDER_ONLY'
                    when match_status = 'FEE_ONLY' then 'FEE_ONLY'
                    when order_settlement_amount <> fee_settlement_amount then 'SETTLEMENT_MISMATCH'
                    when order_commission_amount <> fee_commission_amount then 'COMMISSION_MISMATCH'
                    when (order_settlement_amount - order_commission_amount) <> (fee_settlement_amount - fee_commission_amount) then 'NET_MISMATCH'
                    else null
                end as primary_issue_code,
                order_commission_amount - fee_commission_amount < 0 as refund_candidate,
                match_status in ('ORDER_ONLY', 'FEE_ONLY') as needs_user_input,
                current_timestamp as last_aggregated_at
            from joined
            """;
}
