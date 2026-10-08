export type WorkOrderStatus = 'WAIT_DISPATCH' | 'WAIT_CLAIM' | 'PROCESSING' | 'WAIT_REVIEW' | 'CLOSED'
export interface VersionedCommand { version: number }
export interface RoundCommand extends VersionedCommand { roundNo: number }
export interface WorkOrder { id: string; status: WorkOrderStatus; currentRound: number; version: number; overdue: boolean; canClose: boolean; allowedActions: string[] }
