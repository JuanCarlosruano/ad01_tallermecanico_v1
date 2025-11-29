package org.iesalandalus.programacion.tallermecanico.modelo.datos.ficheros;

import org.iesalandalus.programacion.tallermecanico.modelo.datos.IClientes;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.IFuenteDatos;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.ITrabajos;
import org.iesalandalus.programacion.tallermecanico.modelo.datos.IVehiculos;

public class FuenteDatosFicherosXML implements IFuenteDatos {
    @Override
    public IClientes crearClientes() {
        return Clientes.getInstancia();
    }

    @Override
    public IVehiculos crearVehiculos() {
        return Vehiculos.getInstancia();
    }

    @Override
    public ITrabajos crearTrabajos() {
        return Trabajos.getInstancia();
    }
}
