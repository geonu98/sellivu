function badgeClass(base: string) {
  return `inline-flex rounded-full px-2 py-1 text-xs font-medium ${base}`;
}

export function severityBadgeClass(value: string) {
  switch (value) {
    case "ERROR":
      return badgeClass("bg-red-100 text-red-700");
    case "WARN":
      return badgeClass("bg-yellow-100 text-yellow-700");
    default:
      return badgeClass("bg-slate-100 text-slate-700");
  }
}

export function judgementBadgeClass(value: string) {
  switch (value) {
    case "CONFIRMED":
      return badgeClass("bg-red-100 text-red-700");
    case "EXPLAINABLE":
      return badgeClass("bg-blue-100 text-blue-700");
    default:
      return badgeClass("bg-yellow-100 text-yellow-700");
  }
}

export function matchStatusBadgeClass(value: string) {
  switch (value) {
    case "MATCHED":
      return badgeClass("bg-green-100 text-green-700");
    case "ORDER_ONLY":
    case "FEE_ONLY":
      return badgeClass("bg-red-100 text-red-700");
    case "MISMATCHED":
      return badgeClass("bg-yellow-100 text-yellow-700");
    default:
      return badgeClass("bg-slate-100 text-slate-700");
  }
}

export function reviewStatusBadgeClass(value: string) {
  switch (value) {
    case "OK":
      return badgeClass("bg-green-100 text-green-700");
    case "REVIEW":
      return badgeClass("bg-yellow-100 text-yellow-700");
    case "ISSUE":
      return badgeClass("bg-red-100 text-red-700");
    default:
      return badgeClass("bg-slate-100 text-slate-700");
  }
}