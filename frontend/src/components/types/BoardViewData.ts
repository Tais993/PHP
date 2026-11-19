export type FilterField = 'STATUS' | 'TITLE' | 'DUE_DATE'

export type FilterOperator = 'IN' | 'NOT_IN' | 'CONTAINS' | 'EQUALS'
    | 'LESS_THAN_OR_EQUAL' | 'GREATER_THAN_OR_EQUAL' | 'IS_NULL' | 'IS_NOT_NULL'

export type FilterRule = {
    field: FilterField
    operator: FilterOperator
    values: string[]
}

export type ViewSorting = {
    sortBy: 'TITLE' | 'CREATED_AT'
    sortDirection: 'ASC' | 'DESC'
}

export type BoardViewData = ViewSorting & {
    id: number
    boardId: number
    name: string
    type: string
    groupBy: string
    position: number
    filterRules: FilterRule[]
}
