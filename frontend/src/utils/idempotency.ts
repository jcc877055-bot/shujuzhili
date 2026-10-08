// Keep this key with the original command when retrying; never regenerate on retry.
export const newRequestId = (): string => crypto.randomUUID()
