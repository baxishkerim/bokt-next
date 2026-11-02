-- Срок карты клиента (MM/yy): вводится оператором, уходит в ExpDate получателя (TranzAxis).
alter table credits add column client_card_expiry varchar(8);
