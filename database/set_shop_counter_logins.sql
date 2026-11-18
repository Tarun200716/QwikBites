USE qwikbite;
-- Shop Counter login ID is the exact shop name shown in the dropdown.
-- All 20 seeded shops receive the common demo password.
UPDATE shops SET username = shop_name, password_hash = '123456';

SELECT shop_name AS login_id, password_hash AS demo_password
FROM shops
ORDER BY shop_name;
