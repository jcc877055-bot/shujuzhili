export type ReviewResult = 'PASS' | 'FAIL' | 'ERROR'
export interface ReviewCommand { taskId: string; roundNo: number; version: number; result: ReviewResult; reason: string; proofEvidenceId?: string }
