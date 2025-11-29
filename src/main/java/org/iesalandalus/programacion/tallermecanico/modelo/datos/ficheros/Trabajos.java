package org.iesalandalus.programacion.tallermecanico.modelo.datos.ficheros;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.ITrabajos;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Trabajos implements ITrabajos {

    private static final String FICHERO_TRABAJOS = "datos/ficheros/json/trabajos.json";

    private static Trabajos instancia;
    private final ObjectMapper mapper;

    private Trabajos() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());

        File fichero = new File(FICHERO_TRABAJOS);
        if (!fichero.getParentFile().exists()) {
            fichero.getParentFile().mkdirs();
        }
    }

    public static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }

    @Override
    public void terminar() {

    }

    public List<Trabajo> leer() {
        File fichero = new File(FICHERO_TRABAJOS);
        if (!fichero.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(fichero, new TypeReference<List<Trabajo>>() {});
        } catch (IOException e) {
            System.out.printf("Error al leer el fichero JSON: %s%n", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void escribir(List<Trabajo> trabajos) {
        Objects.requireNonNull(trabajos, "La lista de trabajos no puede ser nula.");
        try {
            mapper.writeValue(new File(FICHERO_TRABAJOS), trabajos);
        } catch (IOException e) {
            System.out.printf("Error al escribir en el fichero JSON: %s%n", e.getMessage());
        }
    }

    @Override
    public List<Trabajo> get() {
        return leer();
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        List<Trabajo> trabajos = leer();
        List<Trabajo> trabajosCliente = new ArrayList<>();
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getCliente().equals(cliente)) {
                trabajosCliente.add(trabajo);
            }
        }
        return trabajosCliente;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        List<Trabajo> trabajos = leer();
        List<Trabajo> trabajosVehiculo = new ArrayList<>();
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getVehiculo().equals(vehiculo)) {
                trabajosVehiculo.add(trabajo);
            }
        }
        return trabajosVehiculo;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        comprobarTrabajo(trabajo.getCliente(), trabajo.getVehiculo(), trabajo.getFechaInicio(), trabajos);

        trabajos.add(trabajo);
        escribir(trabajos);
    }

    private void comprobarTrabajo(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio, List<Trabajo> trabajosExistentes) throws TallerMecanicoExcepcion {
        for (Trabajo trabajo : trabajosExistentes) {
            if (!trabajo.estaCerrado()) {
                if (trabajo.getCliente().equals(cliente)) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo en curso.");
                } else if (trabajo.getVehiculo().equals(vehiculo)) {
                    throw new TallerMecanicoExcepcion("El vehículo está actualmente en el taller.");
                }
            } else {
                if (trabajo.getCliente().equals(cliente) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo posterior.");
                } else if (trabajo.getVehiculo().equals(vehiculo) && !fechaInicio.isAfter(trabajo.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El vehículo tiene otro trabajo posterior.");
                }
            }
        }
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Objects.requireNonNull(mes, "El mes no puede ser nulo.");

        List<Trabajo> trabajos = leer();
        Map<TipoTrabajo, Integer> estadisticas = inicializarEstadisticas();

        for (Trabajo trabajo : trabajos) {
            LocalDate fecha = trabajo.getFechaInicio();
            if (fecha.getMonthValue() == mes.getMonthValue() && fecha.getYear() == mes.getYear()) {
                TipoTrabajo tipoTrabajo = TipoTrabajo.get(trabajo);
                estadisticas.put(tipoTrabajo, estadisticas.get(tipoTrabajo) + 1);
            }
        }
        return estadisticas;
    }

    private Map<TipoTrabajo, Integer> inicializarEstadisticas() {
        Map<TipoTrabajo, Integer> estadisticas = new EnumMap<>(TipoTrabajo.class);
        for (TipoTrabajo tipoTrabajo : TipoTrabajo.values()) {
            estadisticas.put(tipoTrabajo, 0);
        }
        return estadisticas;
    }



    private Trabajo getTrabajoAbierto(Vehiculo vehiculo, List<Trabajo> trabajos) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No puedo operar sobre un vehículo nulo.");

        for (Trabajo trabajo : trabajos) {
            if (trabajo.getVehiculo().equals(vehiculo) && !trabajo.estaCerrado()) {
                return trabajo;
            }
        }
        throw new TallerMecanicoExcepcion("No existe ningún trabajo abierto para dicho vehículo.");
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir horas a un trabajo nulo.");

        List<Trabajo> trabajos = leer();

        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        trabajoEncontrado.anadirHoras(horas);
        escribir(trabajos);

        return trabajoEncontrado;
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir precio del material a un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        if (trabajoEncontrado instanceof Mecanico mecanico) {
            mecanico.anadirPrecioMaterial(precioMaterial);
        } else {
            throw new TallerMecanicoExcepcion("No se puede añadir precio al material para este tipo de trabajos.");
        }

        escribir(trabajos);
        return trabajoEncontrado;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo cerrar un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        trabajoEncontrado.cerrar(fechaFin);
        escribir(trabajos);

        return trabajoEncontrado;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");
        List<Trabajo> trabajos = leer();
        int indice = trabajos.indexOf(trabajo);
        return (indice == -1) ? null : trabajos.get(indice);
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        if (!trabajos.contains(trabajo)) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo igual.");
        }

        trabajos.remove(trabajo);
        escribir(trabajos);
    }
}