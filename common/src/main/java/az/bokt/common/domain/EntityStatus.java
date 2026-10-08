package az.bokt.common.domain;

/**
 * Общий статус активности. Заменяет числовые флаги STATUS 1/0 из старого BOKT
 * (BOKT_USERS.status, BOKT_BRANCHES.status, BOKT_SESSIONS.status) на явные значения.
 */
public enum EntityStatus {
    ACTIVE,
    DISABLED
}
