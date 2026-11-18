# QwikBite — Java Swing + MySQL

A campus food-ordering prototype for Java Food Court and Vendhar Square.

## Requirements
- JDK 17 or newer (JDK 21 recommended)
- MySQL Server running locally on port 3306
- Internet connection for the first JDBC driver download
- Windows PowerShell for `setup.bat`

## Fresh installation
1. Extract the complete `QwikBite` folder.
2. Open MySQL Command Line Client.
3. Import the schema and sample data with `SOURCE C:/Users/User/Downloads/QwikBite/database/qwikbite.sql;` (change the path if needed). **This SQL script drops and recreates the `qwikbite` database**, so it deletes old QwikBite demo data.
4. Verify with `USE qwikbite;`, `SELECT COUNT(*) FROM shops;` (20), and `SELECT COUNT(*) FROM menu_items;` (120).
5. Double-click `setup.bat` to download MySQL Connector/J.
6. Double-click `run.bat` to compile and launch the application.

## Database configuration
`src/main/java/com/qwikbite/config/Database.java` defaults to:
- URL: `jdbc:mysql://localhost:3306/qwikbite`
- User: `root`
- Password: `password`

Change these constants if your local MySQL credentials differ. Do not use these demo credentials in production.

## Roles and demo credentials
- Student, faculty and non-teaching staff: register through the app, then log in with the selected account type.
- Shop Counter accounts: the exact shop name is the login ID; default password for every seeded shop is `123456`.
- BiteMate: `bitemate1`; password `bite123`.

## Ordering flow
1. Choose Java Food Court or Vendhar Square.
2. Choose one outlet.
3. Add items. The ADD button becomes minus / quantity / plus controls.
4. Use the persistent bottom `PLACE ORDER` button to open the detailed bill, or open `VIEW CART` to adjust quantities first.
5. Review item-wise totals, food subtotal, floor-based delivery fee, convenience fee, taxes note and final total.
6. Press `PLACE ORDER` on the bill and choose `Cash to BiteMate` or `UPI to BiteMate`. This is payment at delivery; no online payment gateway is integrated.
7. The chosen payment method is saved with the order, and the confirmation screen displays the selection.

A cart is intentionally limited to one outlet per order, matching the database's single `orders.shop_id` design. Changing the selected food court or outlet clears the current cart to prevent accidentally mixing outlets.

## Delivery fee
- Floors 0–4: ₹10
- Each floor above 4 adds ₹1 (for example, floor 6 = ₹12)
- Convenience fee: ₹0 in this prototype
- Taxes: included in sample listed prices; no additional tax is calculated

## Included functionality
- Student / faculty / staff registration and login
- Shop and BiteMate demo logins
- Two food courts, 20 sample outlets and 120 sample menu items
- Per-item add / increment / decrement controls
- Persistent bottom checkout action and cart dialog
- Detailed fare breakup and final payment-at-delivery selection (cash or UPI)
- Student order history
- Shop order status updates
- BiteMate delivery claiming
- Illustrative campus destination map on order confirmation (not live GPS)

## Notes and limitations
- Menu names and prices are sample prototype data, not verified live outlet menus.
- Passwords are stored as plain text in this academic prototype. Use demo credentials only.
- Cart contents are held in memory until checkout; closing the app discards an unsubmitted cart.
- A cart cannot contain items from multiple outlets because each order belongs to one shop.
- The map is illustrative only. There is no GPS or live location service.
- Live database testing must be performed against the MySQL server on the target computer.


## Shopkeeper operations (new)
- Select the food court and shop; the exact shop name is its login ID. Default password: `123456`.
- `ORDER QUEUE`: refresh incoming orders, accept them, reject them, and mark accepted orders ready for BiteMate pickup.
- `MENU & INVENTORY`: view all items, add new items, edit item name/description/price/stock, toggle item availability, and set the shop OPEN/CLOSED.
- Every menu item starts with 50 units of sample stock. New orders decrement stock inside the same database transaction as order creation. Items with zero stock are hidden from student menus.
- Shop inventory changes are scoped to the logged-in shop.

## BiteMate operations (new)
- Log in with `bitemate1` / `bite123`.
- `AVAILABLE DELIVERIES`: refresh the ready-order pool and accept an unassigned delivery. Assignment uses a conditional update so two BiteMates cannot claim the same order.
- `MY DELIVERIES`: view deliveries assigned to the logged-in BiteMate, mark an order collected, then mark it delivered.
- Completed deliveries remain visible in the list as delivery history.
- The map shown to students remains an illustrative preview, not live GPS tracking.

## Final role workflow
Student places order → Shop accepts → Shop prepares and marks READY → BiteMate accepts → BiteMate marks PICKED_UP → BiteMate marks DELIVERED.

## Authentication options
- Login supports STUDENT, FACULTY, STAFF, SHOP COUNTER, and BITEMATE.
- Sign-up supports student/faculty/staff accounts, BiteMate accounts, and Shop Counter accounts.
- Shop Counter sign-up asks for food court, outlet name, username, and password.
- BiteMate sign-up asks for full name, username, and password.
- After sign-up, the login screen automatically selects the account type just created.
- Login labels and fields use a two-column aligned form layout.

## Latest authentication and BiteMate workflow update

- Shop Counter login and registration use two dependent selectors: **Select food court** then **Select shop**, followed by password. The shop name is resolved by its database ID, not a manually typed name, preventing shop-name mismatch.
- Every seeded Shop Counter account uses its exact shop name as its login ID and `123456` as its default password. Shop Counter registration can still set a different password for the selected outlet.
- BiteMates see newly placed orders in **NEW ORDERS**, can inspect the complete order details before accepting, and can decline an order. A declined order remains available to other BiteMates.
- Accepted deliveries appear in **MY ORDER HISTORY** and must move through `ACCEPTED` -> `ON_WAY_TO_PICKUP` -> `PICKED_UP` -> `OUT_FOR_DELIVERY` -> `DELIVERED`.
- The app requires an image file to mark an order delivered. The delivery image (maximum 8 MB) and filename are stored in the MySQL `orders` row as proof.
- The SQL setup creates `delivery_declines`, `delivery_status`, and delivery-photo columns. Re-import `database/qwikbite.sql` to create the updated schema. **This script drops and recreates the `qwikbite` database, so it deletes existing app data.**

### Shop Counter sign-in
Select `SHOP COUNTER`, choose `Select food court`, choose `Select shop`, then enter that outlet's password. The selected shop name is the login ID, and the default password for all 20 seeded shops is `123456`. There is no shop username field in this flow; the selected outlet is the login identity.

## Final visual and delivery-demo update
- Updated the interface to the **Campus Fresh** palette: deep navy, mint, coral and sunshine accents.
- Added an `OPEN CAMERA (DEMO)` placeholder button to BiteMate delivery history. It intentionally does nothing and does not access the webcam.
- The working `UPLOAD PHOTO & MARK DELIVERED` action remains the delivery-proof workflow: select an image file to upload it and complete the order.
- Standardized the outlet name to **Slice of Life Cafe** (ASCII spelling) to avoid accent/encoding display problems in Swing combo boxes and tables. The JDBC URL explicitly requests UTF-8.
- For an existing database that you want to preserve, run `SOURCE C:/path/to/QwikBite/database/fix_slice_of_life_name.sql;` in MySQL Command Line Client. This updates only that outlet name and does not recreate the database. A fresh import of `database/qwikbite.sql` also uses the corrected name.

### Alternative colour palettes considered
- **Campus Fresh (included):** deep navy `#183247`, mint `#DCF3EB`, coral `#FF6B4A`, sunshine `#FFC857`.
- Berry Pop: aubergine `#40234F`, raspberry `#E94B78`, lavender `#E9DDF5`, soft pink `#FFF1F5`.
- Citrus Market: midnight blue `#19324A`, tangerine `#FF8A34`, lemon `#FFD84D`, pale cream `#FFF8E8`.


### Shop Counter demo credentials
- Select **SHOP COUNTER** on the login screen.
- Select the food court, then select the shop. The selected shop name is the login ID.
- Default password for every seeded shop: `123456`.
- For an existing database, run `SOURCE C:/path/to/QwikBite/database/set_shop_counter_logins.sql;` once.
- For a fresh database, the main `database/qwikbite.sql` already seeds these credentials.

### Existing database: update all Shop Counter credentials without deleting data
Run `SOURCE C:/path/to/QwikBite/database/set_shop_counter_logins.sql;` once. This sets each shop's login ID to its exact shop name and its default password to `123456`; it does not drop or recreate the database.
