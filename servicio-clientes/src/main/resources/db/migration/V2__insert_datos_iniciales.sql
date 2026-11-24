TRUNCATE TABLE clientes RESTART IDENTITY CASCADE;

INSERT INTO clientes (nombre, email, telefono, direccion, creado_en)
VALUES
    ('Juan Pérez', 'juan.perez@tiendaonline.com', '+5493425111222', 'Bv. Gálvez 1250, Santa Fe', now() - INTERVAL '10 days'),
    ('María Gómez', 'maria.gomez@tiendaonline.com', '+5493425333444', 'San Martín 2400, Santa Fe', now() - INTERVAL '8 days'),
    ('Carlos López', 'carlos.lopez@tiendaonline.com', NULL, NULL, now() - INTERVAL '6 days'),
    ('Lucía Fernández', 'lucia.fernandez@tiendaonline.com', '+5493425555666', 'Rivadavia 3100, Santa Fe', now() - INTERVAL '4 days'),
    ('Martín Silva', 'martin.silva@tiendaonline.com', '+5493425777888', 'Urquiza 1950, Santa Fe', now() - INTERVAL '5 days'),
    ('Sofía Torres', 'sofia.torres@tiendaonline.com', NULL, '25 de Mayo 1800, Santa Fe', now() - INTERVAL '1 day');