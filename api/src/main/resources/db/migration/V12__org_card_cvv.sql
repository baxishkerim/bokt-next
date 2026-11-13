-- CVV материнской карты: нужен для операции cardToCard (источник средств).
alter table org_cards add column cvv varchar(8);
