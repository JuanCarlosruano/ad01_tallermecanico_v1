package org.iesalandalus.programacion.tallermecanico.modelo.datos;


public enum FabricaFuenteDatos {

    FICHEROS_XML {
        @Override
        public IFuenteDatos crear() {
            return new FuenteDatosFicherosXML();
        }
    };

    public abstract IFuenteDatos crear();
}
