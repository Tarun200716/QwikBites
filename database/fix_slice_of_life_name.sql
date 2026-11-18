USE qwikbite;
UPDATE shops
SET shop_name = 'Slice of Life Cafe', username = 'Slice of Life Cafe'
WHERE shop_name IN ('Slice of Life Cafe', 'Slice of Life Café') OR username = 'vendhar_1';
