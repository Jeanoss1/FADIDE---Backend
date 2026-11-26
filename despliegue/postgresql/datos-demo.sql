-- =====================================================================================
-- SISCO / FADIDE · Datos de demostración para la sustentación y el despliegue
-- =====================================================================================
-- Cuándo ejecutarlo: DESPUÉS del primer arranque del backend, cuando Hibernate ya creó
-- las tablas y DataSeeder registró los roles y el administrador.
--
--   psql -h 192.168.56.10 -U sisco -d sisco_db -f datos-demo.sql      (desde VM-Backend)
--   sudo -u postgres psql -d sisco_db -f datos-demo.sql                (dentro de VM-BD)
--
-- Es idempotente: si ya existe el contrato FAD-2026-001 no inserta nada.
-- Las fechas son relativas a CURRENT_DATE, de modo que el dashboard (pagos de los últimos
-- 6 meses, licencias por vencer en 30 días) siempre muestra datos vigentes el día de la demo.
-- Todos los nombres y RUC son ficticios.
--
-- Usuarios de prueba que crea (contraseñas cifradas con BCrypt):
--   gestor@fadide.com      Gestor123.       (rol GESTOR)
--   supervisor@fadide.com  Supervisor123.   (rol SUPERVISOR)
--   supervisor2@fadide.com Supervisor123.   (rol SUPERVISOR)
-- El administrador lo crea DataSeeder con ADMIN_CORREO / ADMIN_PASSWORD.
-- =====================================================================================

DO $$
DECLARE
    hoy DATE := CURRENT_DATE;
    r_gestor BIGINT; r_supervisor BIGINT;
    u_sup1 BIGINT; u_sup2 BIGINT;
    c1 BIGINT; c2 BIGINT; c3 BIGINT; c4 BIGINT; c5 BIGINT;
    k1 BIGINT; k2 BIGINT; k3 BIGINT; k4 BIGINT; k5 BIGINT;
    p1 BIGINT; p2 BIGINT; p3 BIGINT; p4 BIGINT; p5 BIGINT; p6 BIGINT;
    t1 BIGINT; t2 BIGINT; t3 BIGINT; t4 BIGINT; t5 BIGINT; t6 BIGINT; t7 BIGINT;
    s1 BIGINT; s2 BIGINT; s3 BIGINT; s4 BIGINT; s5 BIGINT;
BEGIN
    IF EXISTS (SELECT 1 FROM contratos WHERE numero_contrato = 'FAD-2026-001') THEN
        RAISE NOTICE 'Los datos de demostración ya existen; no se insertó nada.';
        RETURN;
    END IF;

    SELECT id_rol INTO r_gestor FROM roles WHERE nombre = 'GESTOR';
    SELECT id_rol INTO r_supervisor FROM roles WHERE nombre = 'SUPERVISOR';
    IF r_gestor IS NULL OR r_supervisor IS NULL THEN
        RAISE EXCEPTION 'Faltan los roles: arranque primero el backend para que DataSeeder los cree.';
    END IF;

    -- ---------- Usuarios ----------
    INSERT INTO usuarios (nombre_completo, correo, documento, password, activo, id_rol) VALUES
        ('Lucía Vargas Céspedes', 'gestor@fadide.com', '45871236', '$2a$10$qxg7UJqm86PpNtPJylb2iO334XbR.u16aCkOQ5PPFfYFCPLrMIKzC', TRUE, r_gestor)
    ON CONFLICT (correo) DO NOTHING;
    INSERT INTO usuarios (nombre_completo, correo, documento, password, activo, id_rol) VALUES
        ('Carlos Huamán Rojas', 'supervisor@fadide.com', '47125893', '$2a$10$COItFreV3NRAZk8GcnroKeP2ZSsegz2ClA3J4xbjBGZHbbXTVxdyu', TRUE, r_supervisor),
        ('Rosa Chávez Llanos', 'supervisor2@fadide.com', '46239871', '$2a$10$COItFreV3NRAZk8GcnroKeP2ZSsegz2ClA3J4xbjBGZHbbXTVxdyu', TRUE, r_supervisor)
    ON CONFLICT (correo) DO NOTHING;
    SELECT id_usuario INTO u_sup1 FROM usuarios WHERE correo = 'supervisor@fadide.com';
    SELECT id_usuario INTO u_sup2 FROM usuarios WHERE correo = 'supervisor2@fadide.com';

    -- ---------- Clientes ----------
    INSERT INTO clientes (razon_social, ruc, direccion, telefono) VALUES ('Inmobiliaria Los Andes Cajamarca S.A.C.', '20601234561', 'Jr. Amalia Puga 450, Cajamarca', '076-361245') RETURNING id_cliente INTO c1;
    INSERT INTO clientes (razon_social, ruc, direccion, telefono) VALUES ('Corporación Educativa San Marcos E.I.R.L.', '20602345672', 'Av. Atahualpa 1020, Cajamarca', '076-342190') RETURNING id_cliente INTO c2;
    INSERT INTO clientes (razon_social, ruc, direccion, telefono) VALUES ('Clínica Santa Apolonia S.A.', '20603456783', 'Jr. Del Comercio 785, Cajamarca', '076-365512') RETURNING id_cliente INTO c3;
    INSERT INTO clientes (razon_social, ruc, direccion, telefono) VALUES ('Agroindustrias Valle Verde S.A.C.', '20604567894', 'Carretera Baños del Inca km 4, Cajamarca', '976543210') RETURNING id_cliente INTO c4;
    INSERT INTO clientes (razon_social, ruc, direccion, telefono) VALUES ('Hotel Cumbe Mayo E.I.R.L.', '20605678905', 'Jr. Silva Santisteban 330, Cajamarca', '076-368877') RETURNING id_cliente INTO c5;

    -- ---------- Contratistas ----------
    INSERT INTO contratistas (nombre, ruc) VALUES ('Estructuras y Concreto del Norte S.A.C.', '20611234567') RETURNING id_contratista INTO k1;
    INSERT INTO contratistas (nombre, ruc) VALUES ('Instalaciones Eléctricas Cajamarca E.I.R.L.', '20612345678') RETURNING id_contratista INTO k2;
    INSERT INTO contratistas (nombre, ruc) VALUES ('Sanitarias y Gasfitería Andina S.R.L.', '20613456789') RETURNING id_contratista INTO k3;
    INSERT INTO contratistas (nombre, ruc) VALUES ('Movimiento de Tierras El Cumbe S.A.C.', '20614567890') RETURNING id_contratista INTO k4;
    INSERT INTO contratistas (nombre, ruc) VALUES ('Acabados y Carpintería Qhapaq E.I.R.L.', '20615678901') RETURNING id_contratista INTO k5;

    -- ---------- Obras (proyectos) ----------
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Edificio Multifamiliar Los Andes', 'Jr. Amalia Puga 450, Cajamarca', 'EN_EJECUCION', c1) RETURNING id_proyecto INTO p1;
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Pabellón de Aulas San Marcos', 'Av. Atahualpa 1020, Cajamarca', 'EN_EJECUCION', c2) RETURNING id_proyecto INTO p2;
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Ampliación Clínica Santa Apolonia', 'Jr. Del Comercio 785, Cajamarca', 'EN_EJECUCION', c3) RETURNING id_proyecto INTO p3;
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Planta de Procesamiento Valle Verde', 'Baños del Inca, Cajamarca', 'PAUSADO', c4) RETURNING id_proyecto INTO p4;
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Remodelación Hotel Cumbe Mayo', 'Jr. Silva Santisteban 330, Cajamarca', 'FINALIZADO', c5) RETURNING id_proyecto INTO p5;
    INSERT INTO proyectos (nombre, ubicacion, estado, id_cliente) VALUES ('Condominio Los Andes II', 'Av. Hoyos Rubio s/n, Cajamarca', 'PLANIFICADO', c1) RETURNING id_proyecto INTO p6;

    INSERT INTO proyecto_contratista (id_proyecto, id_contratista) VALUES
        (p1, k1), (p1, k2), (p1, k3), (p2, k1), (p2, k5), (p3, k1), (p3, k2),
        (p4, k4), (p5, k5), (p5, k2), (p6, k4);

    -- ---------- Licencias (una vencida y dos por vencer en 30 días) ----------
    INSERT INTO licencias (numero, fecha_emision, fecha_vencimiento, id_proyecto) VALUES
        ('LIC-MPC-2026-0145', hoy - 300, hoy + 12, p1),
        ('LIC-MPC-2026-0198', hoy - 250, hoy + 27, p2),
        ('LIC-MPC-2026-0233', hoy - 200, hoy + 165, p3),
        ('LIC-MPC-2025-0871', hoy - 420, hoy - 15, p4),
        ('LIC-MPC-2025-0612', hoy - 500, hoy + 230, p5),
        ('LIC-MPC-2026-0301', hoy - 20, hoy + 345, p6);

    -- ---------- Contratos ----------
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2026-001', hoy - 190, hoy + 120, 480000.00, 'EN_EJECUCION', p1) RETURNING id_contrato INTO t1;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2026-002', hoy - 160, hoy + 10, 315000.00, 'EN_EJECUCION', p2) RETURNING id_contrato INTO t2;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2026-003', hoy - 120, hoy + 200, 620000.00, 'VIGENTE', p3) RETURNING id_contrato INTO t3;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2026-004', hoy - 230, hoy + 60, 270000.00, 'SUSPENDIDO', p4) RETURNING id_contrato INTO t4;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2025-018', hoy - 400, hoy - 40, 185000.00, 'CONFORME', p5) RETURNING id_contrato INTO t5;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2026-005', hoy - 15, hoy + 330, 750000.00, 'VIGENTE', p6) RETURNING id_contrato INTO t6;
    INSERT INTO contratos (numero_contrato, fecha_inicio, fecha_fin, monto_total, estado, id_proyecto) VALUES ('FAD-2025-022', hoy - 380, hoy - 90, 95000.00, 'RESUELTO', p4) RETURNING id_contrato INTO t7;

    -- ---------- Pagos (repartidos en los últimos 6 meses, sin exceder cada contrato) ----------
    INSERT INTO pagos (monto, fecha_pago, estado, conformidad_emitida, id_contrato) VALUES
        (96000.00, hoy - 170, 'PAGADO', TRUE, t1),
        (96000.00, hoy - 125, 'PAGADO', TRUE, t1),
        (72000.00, hoy - 80, 'PAGADO', TRUE, t1),
        (48000.00, hoy - 20, 'PENDIENTE', FALSE, t1),
        (63000.00, hoy - 140, 'PAGADO', TRUE, t2),
        (94500.00, hoy - 95, 'PAGADO', TRUE, t2),
        (63000.00, hoy - 35, 'REGISTRADO', FALSE, t2),
        (124000.00, hoy - 100, 'PAGADO', TRUE, t3),
        (93000.00, hoy - 45, 'PAGADO', TRUE, t3),
        (31000.00, hoy - 8, 'REGISTRADO', FALSE, t3),
        (54000.00, hoy - 150, 'PAGADO', TRUE, t4),
        (27000.00, hoy - 110, 'ANULADO', FALSE, t4),
        (185000.00, hoy - 60, 'PAGADO', TRUE, t5),
        (75000.00, hoy - 5, 'REGISTRADO', FALSE, t6),
        (28500.00, hoy - 175, 'PAGADO', TRUE, t7);

    -- ---------- Penalidades (en UIT) ----------
    INSERT INTO penalidades (monto_uit, fecha_aplicacion, motivo, estado, id_contrato) VALUES
        (0.5000, hoy - 70, 'Retraso de 5 días en el vaciado de losa del tercer piso', 'APLICADA', t1),
        (0.2500, hoy - 40, 'Falta de señalización de seguridad en obra', 'PAGADA', t2),
        (1.0000, hoy - 130, 'Paralización no justificada de los trabajos', 'APLICADA', t4),
        (0.3000, hoy - 200, 'Entrega tardía del expediente de valorización', 'CONDONADA', t5);

    -- ---------- Supervisiones con observaciones y evidencias ----------
    INSERT INTO supervisiones (tipo_autor, fecha_registro, avance_porcentual, observaciones, evidencia_url, id_contrato, id_usuario)
        VALUES ('SUPERVISOR', hoy - 3, 62.50, 'Estructura del cuarto piso concluida; inicia albañilería.', NULL, t1, u_sup1) RETURNING id_supervision INTO s1;
    INSERT INTO supervisiones (tipo_autor, fecha_registro, avance_porcentual, observaciones, evidencia_url, id_contrato, id_usuario)
        VALUES ('SUPERVISOR', hoy - 10, 78.00, 'Tarrajeo de aulas al 80 %; pendiente instalaciones eléctricas.', NULL, t2, u_sup1) RETURNING id_supervision INTO s2;
    INSERT INTO supervisiones (tipo_autor, fecha_registro, avance_porcentual, observaciones, evidencia_url, id_contrato, id_usuario)
        VALUES ('SUPERVISOR', hoy - 18, 34.00, 'Cimentación y columnas del primer nivel terminadas.', NULL, t3, u_sup2) RETURNING id_supervision INTO s3;
    INSERT INTO supervisiones (tipo_autor, fecha_registro, avance_porcentual, observaciones, evidencia_url, id_contrato, id_usuario)
        VALUES ('SUPERVISOR', hoy - 55, 21.00, 'Obra paralizada por suspensión del contrato.', NULL, t4, u_sup2) RETURNING id_supervision INTO s4;
    INSERT INTO supervisiones (tipo_autor, fecha_registro, avance_porcentual, observaciones, evidencia_url, id_contrato, id_usuario)
        VALUES ('SUPERVISOR', hoy - 45, 100.00, 'Obra concluida y recibida conforme por el cliente.', NULL, t5, u_sup1) RETURNING id_supervision INTO s5;

    INSERT INTO observaciones (descripcion, fecha, id_supervision) VALUES
        ('Se recomienda curar el concreto de la losa durante 7 días.', hoy - 3, s1),
        ('Andamios sin barandas en el frente principal; corregir antes de continuar.', hoy - 3, s1),
        ('Verificar la pendiente de las canaletas pluviales del techo.', hoy - 10, s2),
        ('Acero de columnas conforme a planos; sin observaciones mayores.', hoy - 18, s3),
        ('Material expuesto a la intemperie durante la paralización.', hoy - 55, s4);

    INSERT INTO evidencias (imagen, descripcion, fecha, id_supervision) VALUES
        ('https://placehold.co/800x600?text=Losa+4to+piso', 'Losa del cuarto piso después del vaciado', hoy - 3, s1),
        ('https://placehold.co/800x600?text=Aulas+tarrajeo', 'Avance del tarrajeo en aulas', hoy - 10, s2),
        ('https://placehold.co/800x600?text=Columnas+nivel+1', 'Columnas del primer nivel', hoy - 18, s3),
        ('https://placehold.co/800x600?text=Obra+concluida', 'Fachada remodelada del hotel', hoy - 45, s5);

    RAISE NOTICE 'Datos de demostración insertados correctamente.';
END $$;
