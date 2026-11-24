TRUNCATE TABLE reservas, productos RESTART IDENTITY CASCADE;

INSERT INTO productos (sku, nombre, precio, cantidad_disponible)
VALUES
    ('TECL-001', 'Teclado Mecánico RGB', 45000.00, 20),
    ('MOUS-002', 'Mouse Gamer Inalámbrico', 25000.00, 15),
    ('MONI-003', 'Monitor IPS 27 Pulgadas', 180000.00, 5),
    ('AURI-004', 'Auriculares Bluetooth', 35000.00, 0),
    ('WEBC-005', 'Webcam Full HD 1080p', 52000.00, 12),
    ('MICR-006', 'Micrófono Condensador USB', 68000.00, 8),
    ('SILL-007', 'Silla Ergonómica Oficina', 220000.00, 2),
    ('HUBC-008', 'Hub USB-C Multipuerto', 19500.00, 30);

INSERT INTO reservas (producto_id, pedido_id, cantidad, estado, fecha_creacion)
VALUES
    (1, 1, 2, 'ACTIVA', now() - INTERVAL '2 hours'),
    (8, 1, 1, 'ACTIVA', now() - INTERVAL '2 hours'),
    (2, 2, 1, 'ACTIVA', now() - INTERVAL '48 hours'),
    (3, 3, 1, 'ACTIVA', now() - INTERVAL '36 hours'),
    (1, 4, 1, 'ACTIVA', now() - INTERVAL '72 hours'),
    (5, 4, 2, 'ACTIVA', now() - INTERVAL '72 hours'),
    (6, 5, 1, 'ACTIVA', now() - INTERVAL '30 hours'),
    (8, 6, 2, 'LIBERADA', now() - INTERVAL '5 days'),
    (5, 7, 1, 'LIBERADA', now() - INTERVAL '4 days');