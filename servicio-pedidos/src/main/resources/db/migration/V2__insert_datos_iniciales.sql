TRUNCATE TABLE items_pedido, pedidos RESTART IDENTITY CASCADE;

INSERT INTO pedidos (cliente_id, estado, monto_total, fecha_creacion, fecha_actualizacion)
VALUES
    (1, 'CREADO', 109500.00, now() - INTERVAL '2 hours', now() - INTERVAL '2 hours'),
    (2, 'CREADO', 25000.00, now() - INTERVAL '48 hours', now() - INTERVAL '48 hours'),
    (1, 'CONFIRMADO', 180000.00, now() - INTERVAL '36 hours', now() - INTERVAL '35 hours'),
    (3, 'CREADO', 149000.00, now() - INTERVAL '72 hours', now() - INTERVAL '72 hours'),
    (4, 'CREADO', 68000.00, now() - INTERVAL '30 hours', now() - INTERVAL '30 hours'),
    (2, 'CANCELADO', 39000.00, now() - INTERVAL '5 days', now() - INTERVAL '4 days'),
    (5, 'EXPIRADO', 52000.00, now() - INTERVAL '4 days', now() - INTERVAL '3 days');

INSERT INTO items_pedido (pedido_id, producto_id, cantidad, precio_unitario)
VALUES
    (1, 1, 2, 45000.00),
    (1, 8, 1, 19500.00),
    (2, 2, 1, 25000.00),
    (3, 3, 1, 180000.00),
    (4, 1, 1, 45000.00),
    (4, 5, 2, 52000.00),
    (5, 6, 1, 68000.00),
    (6, 8, 2, 19500.00),
    (7, 5, 1, 52000.00);