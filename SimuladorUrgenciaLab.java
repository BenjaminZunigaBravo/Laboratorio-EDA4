
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class SimuladorUrgenciaLab {
static class Paciente{
    private String nombre;
    private String apellido;
    private String ID;
    private int categoria;
    private long tiempoLlegada;
    private String estado;
    private String area;
    private Stack<String> historialCambios;

    public Paciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area){
        this.nombre = nombre;
        this.apellido = apellido;
        this.ID = ID;
        this.categoria = categoria;
        this.tiempoLlegada = tiempoLlegada;
        this.estado = estado;
        this.area = area;
        this.historialCambios = new Stack<>();
    }
    // Getters //
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getID() { return ID; }
    public int getCategoria() { return categoria; }
    public long getTiempoLlegada() { return tiempoLlegada; }
    public String getEstado() { return estado; }
    public String getArea() { return area; }
    public Stack<String> getHistorialCambios() { return historialCambios; }

    // Setters //
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setID(String ID) { this.ID = ID; }
    public void setCategoria(int categoria) { this.categoria = categoria; }
    public void setTiempoLlegada(long tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setArea(String area) { this.area = area; }
    public void setHistorialCambios(Stack<String> historialCambios) { this.historialCambios = historialCambios; }

    // Métodos Extras //
    public long tiempoEsperaActual(){
        return (System.currentTimeMillis() - tiempoLlegada) / (60 * 1000);
    }
    public void registrarCambio(String descripcion){
        historialCambios.push(descripcion);
    }
    public String obtenerUltimoCambio(){
        if(!historialCambios.empty()){
            return historialCambios.pop();
        } else {
            return "No existen cambios registrados";
        }

    }
}
static class AreaAtencion{
    private String nombre;
    private PriorityQueue<Paciente> pacientesHeap;
    private int capacidadMaxima;

    public AreaAtencion(String nombre, int capacidadMaxima){
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        this.pacientesHeap = new PriorityQueue<>(new Comparator<Paciente>(){
            @Override
            public int compare(Paciente p1, Paciente p2){
                int comparadorCategoria = Integer.compare(p1.getCategoria(), p2.getCategoria());
                if (comparadorCategoria != 0){
                    return comparadorCategoria;
                }

                long tiempoEspera1 = p1.tiempoEsperaActual();
                long tiempoEspera2 = p2.tiempoEsperaActual();
                return Long.compare(tiempoEspera2, tiempoEspera1);
            }
        });
    }
    // Métodos Extras //
    public void ingresarPaciente(Paciente p){
        if(pacientesHeap.size() < capacidadMaxima){
            pacientesHeap.add(p);
        }
    }
    public Paciente atenderPaciente(){
        return pacientesHeap.poll();
    }
    public boolean estaSaturada(){
        return pacientesHeap.size() >= capacidadMaxima;
    }
    public List<Paciente> obtenerPacientesPorHeapSort(){
        PriorityQueue<Paciente> auxHeap = new PriorityQueue<>(pacientesHeap);
        List<Paciente> ordenados = new ArrayList<>();

        while(!auxHeap.isEmpty()){
            ordenados.add(auxHeap.poll());
        }

        return ordenados;
    }
}
static class Hospital{
    private Map<String, Paciente> pacientesTotales;
    private PriorityQueue<Paciente> colaAtencion;
    private Map<String, AreaAtencion> areasAtencion;
    private List<Paciente> pacientesAtendidos;

    public Hospital(){
        this.pacientesTotales = new HashMap<>();
        this.colaAtencion = new PriorityQueue<>(new Comparator<Paciente>(){
            @Override
            public int compare(Paciente p1, Paciente p2){
                int comparadorCategoria = Integer.compare(p1.getCategoria(), p2.getCategoria());
                if(comparadorCategoria != 0){
                    return comparadorCategoria;
                }

                long tiempoEspera1 = p1.tiempoEsperaActual();
                long tiempoEspera2 = p2.tiempoEsperaActual();
                return Long.compare(tiempoEspera2, tiempoEspera1);
            }
        });

        this.areasAtencion = new HashMap<>();
        areasAtencion.put("SAPU", new AreaAtencion("SAPU", 100));
        areasAtencion.put("urgencia_adulto", new AreaAtencion("urgencia_adulto", 150));
        areasAtencion.put("infantil", new AreaAtencion("infantil", 50));
        this.pacientesAtendidos = new ArrayList<>();
    }
    // Métodos Extras //
    public void registrarPaciente(Paciente p){
        pacientesTotales.put(p.getID(), p);
        colaAtencion.add(p);
        AreaAtencion area = areasAtencion.get(p.getArea());
        if(area != null && !area.estaSaturada()){
            area.ingresarPaciente(p);
        } else {
            p.setEstado("espera");
        }
        p.registrarCambio("Paciente registrado en la cola de atención");
    }
    public void reasignarCategoria(String id, int nuevaCategoria){
        Paciente p = pacientesTotales.get(id);
        if(p != null){
            p.setCategoria(nuevaCategoria);
            p.registrarCambio("Se cambió a categoría C" + nuevaCategoria);
            colaAtencion.remove(p);
            colaAtencion.add(p);
        }
    }
    public Paciente atenderSiguiente(){
        Paciente p = colaAtencion.poll();
        if(p != null){
            p.setEstado("en_atencion");
            AreaAtencion auxArea = areasAtencion.get(p.getArea());
            if(auxArea != null){
                auxArea.ingresarPaciente(p);
            }
        }
        return p;
    }
    public List<Paciente> obtenerPacientesPorCategoria(int categoria){
        List<Paciente> pacientesPorCategoria = new ArrayList<>();
        for(Paciente p : colaAtencion){
            if(p.getCategoria() == categoria){
                pacientesPorCategoria.add(p);
            }
        }
        return pacientesPorCategoria;
    }
    public AreaAtencion obtenerArea(String nombre){
        return areasAtencion.get(nombre);
    }
}
public static class GeneradorPacientes{
    public Paciente generarPaciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area){
        return new Paciente(nombre, apellido, ID, categoria, tiempoLlegada, estado, area);
    }

}
static class SimuladorUrgencia{

    private Hospital hospital;

    public SimuladorUrgencia(){
        this.hospital = new Hospital();
    }

    public void registrarPaciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area){
        GeneradorPacientes generador = new GeneradorPacientes();
        Paciente p = generador.generarPaciente(nombre, apellido, ID, categoria, tiempoLlegada, estado, area);
        p.registrarCambio("Paciente registrado en el sistema");
        hospital.registrarPaciente(p);
    }

    public void reasignarCategoria(String id, int nuevaCategoria){
        hospital.reasignarCategoria(id, nuevaCategoria);
    }

    public Paciente atenderSiguiente(){
        return hospital.atenderSiguiente();
    }

    public List<Paciente> obtenerPacientesPorCategoria(int categoria){
        return hospital.obtenerPacientesPorCategoria(categoria);
    }

    public AreaAtencion obtenerArea(String nombre){
        return hospital.obtenerArea(nombre);
    }
    public List<Paciente> obtenerPacientesAtendidosPorArea(String area){
        AreaAtencion areaAtencion = hospital.obtenerArea(area);
        if(areaAtencion != null){
            return areaAtencion.obtenerPacientesPorHeapSort();
        }
        return new ArrayList<>();
    }
    public List<Paciente> obtenerPacientesAtendidos(){
        return hospital.pacientesAtendidos;
    }
    public void registrarPacienteAtendido(Paciente p){
        hospital.pacientesAtendidos.add(p);
    }
    public Map<String, Paciente> obtenerPacientesTotales(){
        return hospital.pacientesTotales;
    }
    public PriorityQueue<Paciente> obtenerColaAtencion(){
        return hospital.colaAtencion;
    }
    public Map<String, AreaAtencion> obtenerAreasAtencion(){
        return hospital.areasAtencion;
    }

}
public static void main(String[] args) {
    SimuladorUrgencia simulador = new SimuladorUrgencia();
    
    // Registrar varios pacientes
    simulador.registrarPaciente("Juan", "Perez", "001", 1, System.currentTimeMillis(), "espera", "SAPU");
    simulador.registrarPaciente("Maria", "Lopez", "002", 2, System.currentTimeMillis(), "espera", "urgencia_adulto");
    simulador.registrarPaciente("Pedro", "Gomez", "003", 3, System.currentTimeMillis(), "espera", "infantil");
    simulador.registrarPaciente("Ana", "Martinez", "004", 1, System.currentTimeMillis(), "espera", "SAPU");
    simulador.registrarPaciente("Luis", "Rodriguez", "005", 2, System.currentTimeMillis(), "espera", "urgencia_adulto");
    
    // Reasignar categoría a un paciente
    simulador.reasignarCategoria("001", 2);
    
    // Atender algunos pacientes
    System.out.println("=== Atendiendo pacientes ===");
    for (int i = 0; i < 3; i++) {
        Paciente atendido = simulador.atenderSiguiente();
        if (atendido != null) {
            System.out.println("Atendiendo a: " + atendido.getNombre() + " " + atendido.getApellido() + 
                               " (Categoría: C" + atendido.getCategoria() + ")");
            simulador.registrarPacienteAtendido(atendido);
        }
    }
    
    // Mostrar pacientes por categoría
    System.out.println("\n=== Pacientes en categoría 2 ===");
    List<Paciente> cat2 = simulador.obtenerPacientesPorCategoria(2);
    for (Paciente p : cat2) {
        System.out.println(p.getNombre() + " " + p.getApellido() + " - Tiempo espera: " + 
                         p.tiempoEsperaActual() + " mins");
    }
    
    // Mostrar pacientes atendidos en un área
    System.out.println("\n=== Pacientes atendidos en SAPU ===");
    List<Paciente> sapuAtendidos = simulador.obtenerPacientesAtendidosPorArea("SAPU");
    for (Paciente p : sapuAtendidos) {
        System.out.println(p.getNombre() + " " + p.getApellido());
    }
    
    // Mostrar estado de las áreas
    System.out.println("\n=== Estado de las áreas ===");
    Map<String, AreaAtencion> areas = simulador.obtenerAreasAtencion();
    for (Map.Entry<String, AreaAtencion> entry : areas.entrySet()) {
        AreaAtencion area = entry.getValue();
        System.out.println(area.nombre + ": " + 
                          (area.estaSaturada() ? "Saturada" : "Disponible") + 
                          " (" + area.pacientesHeap.size() + "/" + area.capacidadMaxima + ")");
    }
    
    // Mostrar historial de cambios de un paciente
    System.out.println("\n=== Historial de cambios de paciente 001 ===");
    Paciente paciente001 = simulador.obtenerPacientesTotales().get("001");
    if (paciente001 != null) {
        Stack<String> historial = paciente001.getHistorialCambios();
        while (!historial.empty()) {
            System.out.println(historial.pop());
        }
    }
}
}