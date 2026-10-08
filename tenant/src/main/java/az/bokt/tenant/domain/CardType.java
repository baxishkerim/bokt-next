package az.bokt.tenant.domain;

/**
 * Назначение карты организации:
 * <ul>
 *   <li>{@code MOTHER} — материнская, с неё списываются средства (CHARGE);</li>
 *   <li>{@code PAYOUT} — карта выдачи, на неё сажают деньги (TOPUP), клиент снимает наличные.</li>
 * </ul>
 * Обоих типов может быть несколько (пул карт).
 */
public enum CardType {
    MOTHER,
    PAYOUT
}
