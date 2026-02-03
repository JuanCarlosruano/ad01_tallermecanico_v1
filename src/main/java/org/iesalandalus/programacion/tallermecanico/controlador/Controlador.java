package org.iesalandalus.programacion.tallermecanico.controlador;

import org.iesalandalus.programacion.tallermecanico.modelo.FabricaModelo;
import org.iesalandalus.programacion.tallermecanico.modelo.Modelo;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Trabajo;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.FabricaFuenteDatos;
import org.iesalandalus.programacion.tallermecanico.vista.FabricaVista;
import org.iesalandalus.programacion.tallermecanico.vista.Vista;
import org.iesalandalus.programacion.tallermecanico.vista.eventos.Evento;

import java.util.Objects;

public class Controlador implements IControlador {

    private final Modelo modelo;
    private final Vista vista;

    public Controlador(FabricaModelo fabricaModelo, FabricaFuenteDatos fabricaFuenteDatos, FabricaVista fabricaVista) {
        Objects.requireNonNull(fabricaModelo, "ERROR: La fábrica del modelo no puede ser nula.");
        Objects.requireNonNull(fabricaFuenteDatos, "ERROR: La fábrica de la fuente de datos no puede ser nula.");
        Objects.requireNonNull(fabricaVista, "ERROR: La fábrica de la vista no puede ser nula.");
        this.modelo = fabricaModelo.crear(fabricaFuenteDatos);
        this.vista = fabricaVista.crear();
        this.vista.getGestorEventos().suscribir(this, Evento.values());
    }

    @Override
    public void comenzar() {
        modelo.comenzar();
        vista.comenzar();
    }

    @Override
    public void terminar() {
        modelo.terminar();
        vista.terminar();
    }

    @Override
    public void actualizar(Evento evento) {
        try {
            switch (evento) {
                case INSERTAR_CLIENTE -> { Cliente cliente = vista.leerCliente(); modelo.insertar(cliente); vista.notificarResultado(evento, "Cliente insertado correctamente.", true, cliente); }
                case INSERTAR_VEHICULO -> { Vehiculo vehiculo = vista.leerVehiculo(); modelo.insertar(vehiculo); vista.notificarResultado(evento, "Vehículo insertado correctamente", true, vehiculo); }
                case INSERTAR_REVISION -> { Trabajo trabajo = vista.leerRevision(); modelo.insertar(trabajo); vista.notificarResultado(evento, "Trabajo de revisión insertado correctamente.", true, trabajo); }
                case INSERTAR_MECANICO -> { Trabajo trabajo = vista.leerMecanico(); modelo.insertar(trabajo); vista.notificarResultado(evento, "Trabajo mecánico insertado correctamente.", true, trabajo); }
                case BUSCAR_CLIENTE -> vista.mostrarCliente(modelo.buscar(vista.leerClienteDni()));
                case BUSCAR_VEHICULO -> vista.mostrarVehiculo(modelo.buscar(vista.leerVehiculoMatricula()));
                case BUSCAR_TRABAJO -> vista.mostrarTrabajo(modelo.buscar(vista.leerRevision()));
                case MODIFICAR_CLIENTE -> { Cliente cliente = vista.leerClienteDni(); cliente = modelo.modificar(cliente, vista.leerNuevoNombre(), vista.leerNuevoTelefono()); vista.notificarResultado(evento, "El cliente se ha modificado correctamente.", true, cliente); }
                case ANADIR_HORAS_TRABAJO -> { Trabajo trabajo = vista.leerTrabajoVehiculo(); trabajo = modelo.anadirHoras(trabajo, vista.leerHoras()); vista.notificarResultado(evento, "Horas añadidas correctamente.", true, trabajo); }
                case ANADIR_PRECIO_MATERIAL_TRABAJO -> { Trabajo trabajo = vista.leerTrabajoVehiculo(); trabajo = modelo.anadirPrecioMaterial(trabajo, vista.leerPrecioMaterial()); vista.notificarResultado(evento, "Precio del material añadido correctamente.", true, trabajo); }
                case CERRAR_TRABAJO -> { Trabajo trabajo = vista.leerTrabajoVehiculo(); trabajo = modelo.cerrar(trabajo, vista.leerFechaCierre()); vista.notificarResultado(evento, "Trabajo cerrado correctamente.", true, trabajo); }
                case BORRAR_CLIENTE -> { Cliente cliente = vista.leerClienteDni(); modelo.borrar(cliente); vista.notificarResultado(evento, "Cliente eliminado correctamente.", true, cliente); }
                case BORRAR_VEHICULO -> { Vehiculo vehiculo = vista.leerVehiculoMatricula(); modelo.borrar(vehiculo); vista.notificarResultado(evento, "Vehículo eliminado correctamente.", true, vehiculo); }
                case BORRAR_TRABAJO -> { Trabajo trabajo = vista.leerRevision(); modelo.borrar(trabajo); vista.notificarResultado(evento, "Trabajo eliminado correctamente.", true, trabajo); }
                case LISTAR_CLIENTES -> vista.mostrarClientes(modelo.getClientes());
                case LISTAR_VEHICULOS -> vista.mostrarVehiculos(modelo.getVehiculos());
                case LISTAR_TRABAJOS -> vista.mostrarTrabajos(modelo.getTrabajos());
                case LISTAR_TRABAJOS_CLIENTE -> vista.mostrarTrabajos(modelo.getTrabajos(vista.leerClienteDni()));
                case LISTAR_TRABAJOS_VEHICULO -> vista.mostrarTrabajos(modelo.getTrabajos(vista.leerVehiculoMatricula()));
                case MOSTRAR_ESTADISTICAS_MENSUALES -> vista.mostrarEstadisticasMensuales(modelo.getEstadisticasMensuales(vista.leerMes()));
                case SALIR -> terminar();
            }
        } catch (Exception e) {
            vista.notificarResultado(evento, e.getMessage(), false);
        }
    }

}
