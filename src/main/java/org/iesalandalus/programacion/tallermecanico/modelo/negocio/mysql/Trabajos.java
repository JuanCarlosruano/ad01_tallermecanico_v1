package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Trabajos implements ITrabajos {

    private Connection conexion;
    private static Trabajos instancia;

    // Datos de conexión
    private static final String AWS_ENDPOINT = "ec2-35-153-60-227.compute-1.amazonaws.com";
    private static final String AWS_PORT = "3306";
    private static final String AWS_DB_NAME = "tallermecanico";
    private static final String BD_URL = "jdbc:mysql://" + AWS_ENDPOINT + ":" + AWS_PORT + "/" + AWS_DB_NAME;
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "ad2526";

    private Trabajos() {}

    static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        try {
            conexion = DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos en Trabajos: " + e.getMessage());
        }
    }

    @Override
    public void terminar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    @Override
    public List<Trabajo> get() {
        List<Trabajo> trabajos = new ArrayList<>();
        String sql = "SELECT * FROM Trabajos";

        try (Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {
            while (filas.next()) {
                trabajos.add(reconstruirTrabajo(filas));
            }
        } catch (SQLException e) {
            System.err.println("Error al recuperar los trabajos: " + e.getMessage());
        }
        return trabajos;
    }

    // CORRECCIÓN: Manejo explícito de tipos y excepciones
    private Trabajo reconstruirTrabajo(ResultSet fila) throws SQLException {
        String dni = fila.getString("ClienteDni");
        String matricula = fila.getString("VehiculoMatricula");

        // CORRECCIÓN: Conversión segura de SQL Date a LocalDate
        java.sql.Date fechaInicioSql = fila.getDate("FechaInicio");
        LocalDate fechaInicio = (fechaInicioSql != null) ? fechaInicioSql.toLocalDate() : null;

        // CORRECCIÓN: Uso explícito de java.sql.Date para evitar ambigüedad con java.util.Date
        java.sql.Date fechaFinSql = fila.getDate("FechaFin");
        LocalDate fechaFin = (fechaFinSql != null) ? fechaFinSql.toLocalDate() : null;

        int horas = fila.getInt("Horas");
        float precioMaterial = fila.getFloat("PrecioMaterial");
        String tipo = fila.getString("Tipo");

        // Creamos objetos dummy para buscar los reales
        Cliente cliente = Clientes.getInstancia().buscar(new Cliente("Dummy", dni, "000000000"));
        Vehiculo vehiculo = Vehiculos.getInstancia().buscar(new Vehiculo("Dummy", "Dummy", matricula));

        Trabajo trabajo = null;

        try {
            if ("Revision".equals(tipo)) {
                trabajo = new Revision(cliente, vehiculo, fechaInicio);
            } else if ("Mecanico".equals(tipo)) {
                trabajo = new Mecanico(cliente, vehiculo, fechaInicio);
                // CORRECCIÓN: Capturamos la excepción al añadir material
                ((Mecanico) trabajo).anadirPrecioMaterial(precioMaterial);
            }

            if (trabajo != null) {
                trabajo.anadirHoras(horas);
                if (fechaFin != null) {
                    trabajo.cerrar(fechaFin);
                }
            }
        } catch (TallerMecanicoExcepcion e) {
            // Como los datos vienen de la BD, se supone que son válidos, pero debemos capturar el error.
            System.err.println("Error de integridad al reconstruir trabajo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }

        return trabajo;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");

        comprobarTrabajo(trabajo.getCliente(), trabajo.getVehiculo(), trabajo.getFechaInicio());

        String sql = "INSERT INTO Trabajos (ClienteDni, VehiculoMatricula, FechaInicio, Tipo) VALUES (?, ?, ?, ?)";
        String tipo = (trabajo instanceof Revision) ? "Revision" : "Mecanico";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, trabajo.getCliente().getDni());
            sentencia.setString(2, trabajo.getVehiculo().matricula());
            sentencia.setDate(3, java.sql.Date.valueOf(trabajo.getFechaInicio()));
            sentencia.setString(4, tipo);
            sentencia.executeUpdate();

            if (trabajo instanceof Mecanico mecanico && mecanico.getPrecioMaterial() > 0) {
                anadirPrecioMaterial(trabajo, mecanico.getPrecioMaterial());
            }
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar el trabajo: " + e.getMessage());
        }
    }

    private void comprobarTrabajo(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio) throws TallerMecanicoExcepcion {
        String sqlAbierto = "SELECT COUNT(*) FROM Trabajos WHERE (ClienteDni = ? OR VehiculoMatricula = ?) AND FechaFin IS NULL";

        try (PreparedStatement sentencia = conexion.prepareStatement(sqlAbierto)) {
            sentencia.setString(1, cliente.getDni());
            sentencia.setString(2, vehiculo.matricula());
            ResultSet rs = sentencia.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new TallerMecanicoExcepcion("El cliente o el vehículo ya tienen un trabajo en curso.");
            }
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al comprobar trabajos abiertos.");
        }
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");
        String sql = "SELECT * FROM Trabajos WHERE ClienteDni = ? AND VehiculoMatricula = ? AND FechaInicio = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, trabajo.getCliente().getDni());
            sentencia.setString(2, trabajo.getVehiculo().matricula());
            sentencia.setDate(3, java.sql.Date.valueOf(trabajo.getFechaInicio()));
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return reconstruirTrabajo(filas);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar el trabajo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo igual.");
        }
        String sql = "DELETE FROM Trabajos WHERE ClienteDni = ? AND VehiculoMatricula = ? AND FechaInicio = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, trabajo.getCliente().getDni());
            sentencia.setString(2, trabajo.getVehiculo().matricula());
            sentencia.setDate(3, java.sql.Date.valueOf(trabajo.getFechaInicio()));
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al borrar el trabajo.");
        }
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        List<Trabajo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Trabajos WHERE ClienteDni = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, cliente.getDni());
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(reconstruirTrabajo(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        List<Trabajo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Trabajos WHERE VehiculoMatricula = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, vehiculo.matricula());
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(reconstruirTrabajo(rs));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return lista;
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Trabajo trabajoBD = getTrabajoAbierto(trabajo.getVehiculo());

        String sql = "UPDATE Trabajos SET Horas = Horas + ? WHERE ClienteDni = ? AND VehiculoMatricula = ? AND FechaInicio = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setInt(1, horas);
            st.setString(2, trabajoBD.getCliente().getDni());
            st.setString(3, trabajoBD.getVehiculo().matricula());
            st.setDate(4, java.sql.Date.valueOf(trabajoBD.getFechaInicio()));
            st.executeUpdate();
            trabajoBD.anadirHoras(horas);
            return trabajoBD;
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al añadir horas.");
        }
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Trabajo trabajoBD = getTrabajoAbierto(trabajo.getVehiculo());
        if (!(trabajoBD instanceof Mecanico)) {
            throw new TallerMecanicoExcepcion("No se puede añadir material a una revisión.");
        }

        String sql = "UPDATE Trabajos SET PrecioMaterial = PrecioMaterial + ? WHERE ClienteDni = ? AND VehiculoMatricula = ? AND FechaInicio = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setFloat(1, precioMaterial);
            st.setString(2, trabajoBD.getCliente().getDni());
            st.setString(3, trabajoBD.getVehiculo().matricula());
            st.setDate(4, java.sql.Date.valueOf(trabajoBD.getFechaInicio()));
            st.executeUpdate();
            ((Mecanico) trabajoBD).anadirPrecioMaterial(precioMaterial);
            return trabajoBD;
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al añadir precio material.");
        }
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Trabajo trabajoBD = getTrabajoAbierto(trabajo.getVehiculo());

        String sql = "UPDATE Trabajos SET FechaFin = ? WHERE ClienteDni = ? AND VehiculoMatricula = ? AND FechaInicio = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setDate(1, java.sql.Date.valueOf(fechaFin));
            st.setString(2, trabajoBD.getCliente().getDni());
            st.setString(3, trabajoBD.getVehiculo().matricula());
            st.setDate(4, java.sql.Date.valueOf(trabajoBD.getFechaInicio()));
            st.executeUpdate();
            trabajoBD.cerrar(fechaFin);
            return trabajoBD;
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al cerrar el trabajo.");
        }
    }

    private Trabajo getTrabajoAbierto(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        String sql = "SELECT * FROM Trabajos WHERE VehiculoMatricula = ? AND FechaFin IS NULL";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, vehiculo.matricula());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return reconstruirTrabajo(rs);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        throw new TallerMecanicoExcepcion("No existe ningún trabajo abierto para dicho vehículo.");
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Map<TipoTrabajo, Integer> estadisticas = new EnumMap<>(TipoTrabajo.class);
        for (TipoTrabajo tipo : TipoTrabajo.values()) estadisticas.put(tipo, 0);

        String sql = "SELECT Tipo, COUNT(*) as Cantidad FROM Trabajos WHERE MONTH(FechaInicio) = ? AND YEAR(FechaInicio) = ? GROUP BY Tipo";

        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setInt(1, mes.getMonthValue());
            st.setInt(2, mes.getYear());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String tipoStr = rs.getString("Tipo");
                int cantidad = rs.getInt("Cantidad");
                if (tipoStr.equalsIgnoreCase("Mecanico")) {
                    estadisticas.put(TipoTrabajo.MECANICO, cantidad);
                } else if (tipoStr.equalsIgnoreCase("Revision")) {
                    estadisticas.put(TipoTrabajo.REVISION, cantidad);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en estadísticas: " + e.getMessage());
        }
        return estadisticas;
    }
}