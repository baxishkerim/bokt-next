-- Базовые валюты (ISO 4217). Платформенный тенант и супер-админ создаются
-- бутстрап-раннером (DataInitializer) через реальный PasswordEncoder.

insert into currencies (created_at, updated_at, code, value) values
    (now(), now(), '944', 'AZN'),
    (now(), now(), '840', 'USD'),
    (now(), now(), '978', 'EUR');
