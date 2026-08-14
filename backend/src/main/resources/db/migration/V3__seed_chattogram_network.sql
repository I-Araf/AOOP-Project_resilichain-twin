-- Frozen demo scenario per docs/ResiliChain_Twin_Build_Blueprint.md Section 1:
-- Port Chattogram, 36-hour closure, 12 shipments, 3 exposed warehouses.
-- Node ids are assigned explicitly so routes/shipments below can reference them directly;
-- sequences are re-synced at the end so future app-generated inserts don't collide.

-- Suppliers (raw material sources)
INSERT INTO network_node (id, node_type, name, latitude, longitude, status, created_at) VALUES
    (1, 'SUPPLIER', 'Narayanganj Textile Mills', 23.6238, 90.5000, 'OPERATIONAL', CURRENT_TIMESTAMP),
    (2, 'SUPPLIER', 'Gazipur Yarn Suppliers',    23.9999, 90.4203, 'OPERATIONAL', CURRENT_TIMESTAMP);

INSERT INTO supplier (id, reliability_rating, lead_time_capability_days) VALUES
    (1, 0.92, 4),
    (2, 0.88, 6);

-- Factories (manufacturing)
INSERT INTO network_node (id, node_type, name, latitude, longitude, status, created_at) VALUES
    (3, 'FACTORY', 'Dhaka Garments Factory',     23.8103, 90.4125, 'OPERATIONAL', CURRENT_TIMESTAMP),
    (4, 'FACTORY', 'Chattogram EPZ Factory',     22.3150, 91.7832, 'OPERATIONAL', CURRENT_TIMESTAMP);

INSERT INTO factory (id, production_capacity, current_output) VALUES
    (3, 8000, 6200),
    (4, 6000, 5100);

-- Port (the disruption target for the frozen scenario)
INSERT INTO network_node (id, node_type, name, latitude, longitude, status, created_at) VALUES
    (5, 'PORT', 'Port of Chattogram', 22.3384, 91.8317, 'OPERATIONAL', CURRENT_TIMESTAMP);

INSERT INTO port (id, throughput_capacity, operational_status) VALUES
    (5, 9000, 'OPEN');

-- Warehouses (the 3 exposed when the port closes)
INSERT INTO network_node (id, node_type, name, latitude, longitude, status, created_at) VALUES
    (6, 'WAREHOUSE', 'Chattogram Distribution Warehouse', 22.3569, 91.7832, 'OPERATIONAL', CURRENT_TIMESTAMP),
    (7, 'WAREHOUSE', 'Dhaka Central Warehouse',           23.7808, 90.4045, 'OPERATIONAL', CURRENT_TIMESTAMP),
    (8, 'WAREHOUSE', 'Sylhet Regional Warehouse',         24.8949, 91.8687, 'OPERATIONAL', CURRENT_TIMESTAMP);

INSERT INTO warehouse (id, capacity, current_stock) VALUES
    (6, 12000, 7400),
    (7, 15000, 9800),
    (8, 6000, 3200);

-- Routes: supplier -> factory -> port -> warehouse, plus one direct factory -> warehouse lane.
-- Routes 5-9 all touch the port; shipments below are placed only on those five so that
-- disrupting the port later (Stage 6) affects exactly 12 shipments across exactly 3 warehouses.
INSERT INTO route (id, origin_id, destination_id, cost, lead_time_hours, capacity) VALUES
    (1, 1, 3, 1200.00, 18, 2000),  -- Narayanganj Textile Mills -> Dhaka Garments Factory
    (2, 2, 3, 900.00,  22, 1800),  -- Gazipur Yarn Suppliers -> Dhaka Garments Factory
    (3, 2, 4, 1500.00, 30, 1600),  -- Gazipur Yarn Suppliers -> Chattogram EPZ Factory
    (4, 1, 4, 1700.00, 28, 1600),  -- Narayanganj Textile Mills -> Chattogram EPZ Factory
    (5, 3, 5, 2200.00, 14, 3000),  -- Dhaka Garments Factory -> Port of Chattogram
    (6, 4, 5, 800.00,  6,  3500),  -- Chattogram EPZ Factory -> Port of Chattogram
    (7, 5, 6, 600.00,  4,  4000),  -- Port of Chattogram -> Chattogram Distribution Warehouse
    (8, 5, 7, 2400.00, 16, 4000),  -- Port of Chattogram -> Dhaka Central Warehouse
    (9, 5, 8, 3100.00, 22, 2500),  -- Port of Chattogram -> Sylhet Regional Warehouse
    (10, 3, 7, 1900.00, 12, 2000); -- Dhaka Garments Factory -> Dhaka Central Warehouse (bypasses port)

-- 12 shipments, all on port-touching routes (5-9): 3 warehouses exposed, matches the deck.
INSERT INTO shipment (id, route_id, origin_id, destination_id, status, quantity, eta) VALUES
    (1,  5, 3, 5, 'PLANNED', 420, CURRENT_TIMESTAMP + INTERVAL '18 hours'),
    (2,  5, 3, 5, 'PLANNED', 380, CURRENT_TIMESTAMP + INTERVAL '30 hours'),
    (3,  6, 4, 5, 'PLANNED', 510, CURRENT_TIMESTAMP + INTERVAL '10 hours'),
    (4,  6, 4, 5, 'PLANNED', 275, CURRENT_TIMESTAMP + INTERVAL '20 hours'),
    (5,  7, 5, 6, 'PLANNED', 300, CURRENT_TIMESTAMP + INTERVAL '8 hours'),
    (6,  7, 5, 6, 'PLANNED', 260, CURRENT_TIMESTAMP + INTERVAL '14 hours'),
    (7,  7, 5, 6, 'PLANNED', 190, CURRENT_TIMESTAMP + INTERVAL '26 hours'),
    (8,  8, 5, 7, 'PLANNED', 640, CURRENT_TIMESTAMP + INTERVAL '22 hours'),
    (9,  8, 5, 7, 'PLANNED', 355, CURRENT_TIMESTAMP + INTERVAL '34 hours'),
    (10, 8, 5, 7, 'PLANNED', 480, CURRENT_TIMESTAMP + INTERVAL '40 hours'),
    (11, 9, 5, 8, 'PLANNED', 220, CURRENT_TIMESTAMP + INTERVAL '30 hours'),
    (12, 9, 5, 8, 'PLANNED', 175, CURRENT_TIMESTAMP + INTERVAL '46 hours');

-- Re-sync identity sequences so future app-generated inserts continue past these explicit ids.
SELECT setval(pg_get_serial_sequence('network_node', 'id'), (SELECT MAX(id) FROM network_node));
SELECT setval(pg_get_serial_sequence('route', 'id'), (SELECT MAX(id) FROM route));
SELECT setval(pg_get_serial_sequence('shipment', 'id'), (SELECT MAX(id) FROM shipment));
