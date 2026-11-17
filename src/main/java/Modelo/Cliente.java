package Modelo;

public class Cliente {
    private int idCliente;
    private String documentoDni;
    private String apellidos;
    private String nombres;
    private String correoElectronico;
    private String numeroCelular;
    private String direccionDomicilio;

    public Cliente() {
        // Valores vacios (y no null) para que los formularios nunca muestren "null"
        this.documentoDni = "";
        this.apellidos = "";
        this.nombres = "";
        this.correoElectronico = "";
        this.numeroCelular = "";
        this.direccionDomicilio = "";
    }

    public Cliente(int idCliente, String documentoDni, String apellidos, String nombres,
                   String correoElectronico, String numeroCelular, String direccionDomicilio) {
        this.idCliente = idCliente;
        this.documentoDni = documentoDni;
        this.apellidos = apellidos;
        this.nombres = nombres;
        this.correoElectronico = correoElectronico;
        this.numeroCelular = numeroCelular;
        this.direccionDomicilio = direccionDomicilio;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getDocumentoDni() {
        return documentoDni;
    }

    public void setDocumentoDni(String documentoDni) {
        this.documentoDni = documentoDni;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public String getDireccionDomicilio() {
        return direccionDomicilio;
    }

    public void setDireccionDomicilio(String direccionDomicilio) {
        this.direccionDomicilio = direccionDomicilio;
    }

    /** "Apellidos, Nombres": se usa en el mensaje del modal de eliminar. */
    public String getNombreCompleto() {
        return apellidos + ", " + nombres;
    }
}
