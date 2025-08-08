-- BaseDatos.sql 
-- Esquema de tablas para ambos microservicios en PostgreSQL.
-- Ejecuta cada sección en su base de datos correspondiente:
--   * CUSTOMER  -> customerdb
--   * ACCOUNT   -> accountdb
-- No se crean las bases de datos aquí para no requerir superusuario.

------------------------------------------------------------
-- =============  CUSTOMER (BD: customerdb)  =============
------------------------------------------------------------

-- Personas (clase base de la herencia JOINED)
CREATE TABLE IF NOT EXISTS persona (
  id               SERIAL PRIMARY KEY,
  nombre           VARCHAR(120),
  genero           VARCHAR(10),
  edad             INT,
  identificacion   VARCHAR(20) UNIQUE NOT NULL,
  direccion        VARCHAR(200),
  telefono         VARCHAR(20)
);

-- Clientes (extiende persona vía JOINED: PK = FK)
CREATE TABLE IF NOT EXISTS cliente (
  id           BIGINT PRIMARY KEY REFERENCES persona(id) ON DELETE CASCADE,
  cliente_id   VARCHAR(30) UNIQUE NOT NULL,
  password     VARCHAR(120),
  estado       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_persona_identificacion ON persona(identificacion);
CREATE INDEX IF NOT EXISTS idx_cliente_cliente_id     ON cliente(cliente_id);

------------------------------------------------------------
-- ==============  ACCOUNT (BD: accountdb)  ==============
------------------------------------------------------------

-- Cuentas
CREATE TABLE IF NOT EXISTS cuenta (
  numero          VARCHAR(30) PRIMARY KEY,
  tipo            VARCHAR(20)  NOT NULL,         -- AHORROS | CORRIENTE
  saldo_inicial   NUMERIC(14,2) NOT NULL DEFAULT 0,
  estado          BOOLEAN NOT NULL DEFAULT TRUE,
  -- Snapshot del cliente para desacoplar bounded contexts
  cliente_id      VARCHAR(30)  NOT NULL,
  cliente_nombre  VARCHAR(120) NOT NULL,
  CONSTRAINT chk_cuenta_tipo CHECK (tipo IN ('AHORROS','CORRIENTE'))
);

-- Movimientos
CREATE TABLE IF NOT EXISTS movimiento (
  id             SERIAL PRIMARY KEY,
  fecha          DATE NOT NULL,
  tipo           VARCHAR(20) NOT NULL,           -- DEPOSITO | RETIRO
  valor          NUMERIC(14,2) NOT NULL,
  saldo          NUMERIC(14,2) NOT NULL,
  cuenta_numero  VARCHAR(30) NOT NULL REFERENCES cuenta(numero) ON DELETE CASCADE,
  CONSTRAINT chk_mov_tipo   CHECK (tipo IN ('DEPOSITO','RETIRO')),
  CONSTRAINT chk_mov_valor  CHECK (valor <> 0)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id            ON cuenta(cliente_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_cuenta_numero     ON movimiento(cuenta_numero);
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha             ON movimiento(fecha);

-- Fin del script
