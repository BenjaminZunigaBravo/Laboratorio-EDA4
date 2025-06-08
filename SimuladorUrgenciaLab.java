import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SimuladorUrgenciaLab {

    static class Paciente {
        private String nombre;
        private String apellido;
        private String ID;
        private int categoria;
        private long tiempoLlegada;
        private String estado;
        private String area;
        private long tiempoAtencion;
        private Stack<String> historialCambios;

        public Paciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.ID = ID;
            this.categoria = categoria;
            this.tiempoLlegada = tiempoLlegada;
            this.estado = estado;
            this.area = area;
            this.historialCambios = new Stack<>();
        }

        // Getters
        public String getNombre() { return nombre; }
        public long getTiempoAtencion() { return tiempoAtencion; }
        public String getApellido() { return apellido; }
        public String getID() { return ID; }
        public int getCategoria() { return categoria; }
        public long getTiempoLlegada() { return tiempoLlegada; }
        public String getEstado() { return estado; }
        public String getArea() { return area; }
        public Stack<String> getHistorialCambios() { return historialCambios; }

        // Setters
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setTiempoAtencion(long tiempoAtencion) { this.tiempoAtencion = tiempoAtencion; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public void setID(String ID) { this.ID = ID; }
        public void setCategoria(int categoria) { this.categoria = categoria; }
        public void setTiempoLlegada(long tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
        public void setEstado(String estado) { this.estado = estado; }
        public void setArea(String area) { this.area = area; }
        public void setHistorialCambios(Stack<String> historialCambios) { this.historialCambios = historialCambios; }

        // Métodos Extras
        public long tiempoEsperaActual() {
            return (System.currentTimeMillis() - tiempoLlegada) / (60 * 1000);
        }

        public void registrarCambio(String descripcion) {
            historialCambios.push(descripcion);
        }

        public String obtenerUltimoCambio() {
            if (!historialCambios.empty()) {
                return historialCambios.pop();
            } else {
                return "No existen cambios registrados";
            }
        }
    }

    static class AreaAtencion {
        private String nombre;
        private PriorityQueue<Paciente> pacientesHeap;
        private int capacidadMaxima;

        public AreaAtencion(String nombre, int capacidadMaxima) {
            this.nombre = nombre;
            this.capacidadMaxima = capacidadMaxima;
            this.pacientesHeap = new PriorityQueue<>(new Comparator<Paciente>() {
                @Override
                public int compare(Paciente p1, Paciente p2) {
                    int comparadorCategoria = Integer.compare(p1.getCategoria(), p2.getCategoria());
                    if (comparadorCategoria != 0) {
                        return comparadorCategoria;
                    }
                    long tiempoEspera1 = p1.tiempoEsperaActual();
                    long tiempoEspera2 = p2.tiempoEsperaActual();
                    return Long.compare(tiempoEspera2, tiempoEspera1);
                }
            });
        }

        public void ingresarPaciente(Paciente p) {
            if (pacientesHeap.size() < capacidadMaxima) {
                pacientesHeap.add(p);
            }
        }

        public Paciente atenderPaciente() {
            return pacientesHeap.poll();
        }

        public boolean estaSaturada() {
            return pacientesHeap.size() >= capacidadMaxima;
        }

        public List<Paciente> obtenerPacientesPorHeapSort() {
            PriorityQueue<Paciente> auxHeap = new PriorityQueue<>(pacientesHeap);
            List<Paciente> ordenados = new ArrayList<>();
            while (!auxHeap.isEmpty()) {
                ordenados.add(auxHeap.poll());
            }
            return ordenados;
        }
    }

    static class Hospital {
        private Map<String, Paciente> pacientesTotales;
        private PriorityQueue<Paciente> colaAtencion;
        private Map<String, AreaAtencion> areasAtencion;
        private List<Paciente> pacientesAtendidos;

        public Hospital() {
            this.pacientesTotales = new HashMap<>();
            this.colaAtencion = new PriorityQueue<>(new Comparator<Paciente>() {
                @Override
                public int compare(Paciente p1, Paciente p2) {
                    int comparadorCategoria = Integer.compare(p1.getCategoria(), p2.getCategoria());
                    if (comparadorCategoria != 0) {
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

        public void registrarPaciente(Paciente p) {
            pacientesTotales.put(p.getID(), p);
            colaAtencion.add(p);
            AreaAtencion area = areasAtencion.get(p.getArea());
            if (area != null && !area.estaSaturada()) {
                area.ingresarPaciente(p);
            } else {
                p.setEstado("espera");
            }
            p.registrarCambio("Paciente registrado en la cola de atención");
        }

        public void reasignarCategoria(String id, int nuevaCategoria) {
            Paciente p = pacientesTotales.get(id);
            if (p != null) {
                p.setCategoria(nuevaCategoria);
                p.registrarCambio("Se cambió a categoría C" + nuevaCategoria);
                colaAtencion.remove(p);
                colaAtencion.add(p);
            }
        }

        public Paciente atenderSiguiente() {
            Paciente p = colaAtencion.poll();
            if (p != null) {
                p.setEstado("en_atencion");
                p.setTiempoAtencion(System.currentTimeMillis());
                AreaAtencion auxArea = areasAtencion.get(p.getArea());
                if (auxArea != null) {
                    auxArea.ingresarPaciente(p);
                }
            }
            return p;
        }

        public List<Paciente> obtenerPacientesPorCategoria(int categoria) {
            List<Paciente> pacientesPorCategoria = new ArrayList<>();
            for (Paciente p : colaAtencion) {
                if (p.getCategoria() == categoria) {
                    pacientesPorCategoria.add(p);
                }
            }
            return pacientesPorCategoria;
        }

        public AreaAtencion obtenerArea(String nombre) {
            return areasAtencion.get(nombre);
        }

        public PriorityQueue<Paciente> obtenerColaAtencion() {
            return colaAtencion;
        }

        public List<Paciente> obtenerPacientesAtendidos() {
            return pacientesAtendidos;
        }
    }

    public static class GeneradorPacientes {
        public Paciente generarPaciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area) {
            return new Paciente(nombre, apellido, ID, categoria, tiempoLlegada, estado, area);
        }
        public int generarCategoriaAleatoria() {
            double probabilidad = Math.random() * 100;
            if (probabilidad < 10) {
                return 1;
            } else if (probabilidad < 25) {
                return 2;
            } else if (probabilidad < 43) {
                return 3;
            } else if (probabilidad < 70) {
                return 4;
            } else {
                return 5;
            }
        }
        public String generarAreaAleatoria() {
            String[] areas = {"SAPU", "urgencia_adulto", "infantil"};
            int indice = (int) (Math.random() * areas.length);
            return areas[indice];
        }
        public Paciente generarPacienteAleatorio(int minutoActual, long tiempoLlegada) {
            String nombre = "Nombre" + (minutoActual / 10 + 1);
            String apellido = "Apellido" + (minutoActual / 10 + 1);
            String id = "ID" + (minutoActual / 10 + 1);
            int categoria = generarCategoriaAleatoria();
            String area = generarAreaAleatoria();
            return new Paciente(nombre, apellido, id, categoria, tiempoLlegada, "espera", area);
        }
    }

    static class SimuladorUrgencia {
        private Hospital hospital;
        private GeneradorPacientes generador;
        private long tiempoBase; 

        public SimuladorUrgencia() {
            this.hospital = new Hospital();
            this.generador = new GeneradorPacientes();
            this.tiempoBase = System.currentTimeMillis(); 
        }

        public void registrarPaciente(String nombre, String apellido, String ID, int categoria, long tiempoLlegada, String estado, String area) {
            Paciente p = generador.generarPaciente(nombre, apellido, ID, categoria, tiempoLlegada, estado, area);
            p.registrarCambio("Paciente registrado en el sistema");
            hospital.registrarPaciente(p);
        }
        public void registrarPaciente(Paciente p) {
            p.registrarCambio("Paciente registrado en el sistema");
            hospital.registrarPaciente(p);
        }

        public void reasignarCategoria(String id, int nuevaCategoria) {
            hospital.reasignarCategoria(id, nuevaCategoria);
        }

        public Paciente atenderSiguiente() {
            return hospital.atenderSiguiente();
        }

        public List<Paciente> obtenerPacientesPorCategoria(int categoria) {
            return hospital.obtenerPacientesPorCategoria(categoria);
        }

        public AreaAtencion obtenerArea(String nombre) {
            return hospital.obtenerArea(nombre);
        }

        public List<Paciente> obtenerPacientesAtendidosPorArea(String area) {
            AreaAtencion areaAtencion = hospital.obtenerArea(area);
            if (areaAtencion != null) {
                return areaAtencion.obtenerPacientesPorHeapSort();
            }
            return new ArrayList<>();
        }

        public List<Paciente> obtenerPacientesAtendidos() {
            return hospital.pacientesAtendidos;
        }

        public void registrarPacienteAtendido(Paciente p) {
            hospital.pacientesAtendidos.add(p);
        }

        public Map<String, Paciente> obtenerPacientesTotales() {
            return hospital.pacientesTotales;
        }

        public PriorityQueue<Paciente> obtenerColaAtencion() {
            return hospital.colaAtencion;
        }

        public Map<String, AreaAtencion> obtenerAreasAtencion() {
            return hospital.areasAtencion;
        }

        public void atenderPacientes(int cantidad, long tiempoSimulado) {
            for (int i = 0; i < cantidad; i++) {
                Paciente atendido = atenderSiguiente();
                if (atendido != null) {
                    atendido.setTiempoAtencion(tiempoSimulado); 
                    registrarPacienteAtendido(atendido);
                    atendido.registrarCambio("El paciente " + atendido.getID() + " ha sido atendido en el área " + atendido.getArea());
                }
            }
        }
        public void simular(int pacientePorDia) {
            long tiempoSimulacion = 24 * 60;
            int contadorPacientes = 0;
            int contadorNuevosIngresos = 0;

            for (int minutoActual = 0; minutoActual < tiempoSimulacion; minutoActual++) {
                long tiempoSimulado = tiempoBase + minutoActual * 60 * 1000; 

                if (minutoActual % 10 == 0 && contadorPacientes < pacientePorDia) {
                    Paciente nuevoPaciente = generador.generarPacienteAleatorio(minutoActual, tiempoSimulado);
                    registrarPaciente(nuevoPaciente);
                    contadorPacientes++;
                    contadorNuevosIngresos++;
                    if (contadorNuevosIngresos >= 3) {
                        atenderPacientes(2, tiempoSimulado);
                        contadorNuevosIngresos = 0;
                    }
                }
                if (minutoActual % 15 == 0) {
                    atenderPacientes(1, tiempoSimulado);
                }
                verificarTiemposEsperaExcedidos(minutoActual, tiempoSimulado);
            }
            generarReporte();
        }
        public void verificarTiemposEsperaExcedidos(int minutoActual, long tiempoSimulado) {
            List<Paciente> pacientesParaRevision = new ArrayList<>(hospital.obtenerColaAtencion());
        for (Paciente p : pacientesParaRevision) {
            long tiempoEspera = (tiempoSimulado - p.getTiempoLlegada()) / (60 * 1000);
            int tiempoMaximo = obtenerTiempoMaximoCategoria(p.getCategoria());
            if (tiempoEspera > tiempoMaximo) {
                reasignarCategoria(p.getID(), 1);
                p.registrarCambio("Dando prioridad a paciente por tiempo de espera excedido");
            }
        }
    }
    public int obtenerTiempoMaximoCategoria(int categoria){
        return 15;
    }
    public void generarReporte(){
        int totalPacientes = hospital.obtenerPacientesAtendidos().size();
        long sumaTiempoEspera = 0;
        int [] pacientesPorCategoria = new int[5];
        int recategorizados = 0;
        if (totalPacientes == 0) {
            System.out.println("No se han atendido pacientes.");
            return;
        }
        List<Paciente> atendidosCopia = new ArrayList<>(hospital.obtenerPacientesAtendidos());
        for (Paciente p : atendidosCopia){
            int tiempoEspera = (int)((p.getTiempoAtencion() - p.getTiempoLlegada()) / (60 * 1000));
            sumaTiempoEspera += tiempoEspera;
            if (p.getCategoria() >= 1 && p.getCategoria() <= 5) {
                pacientesPorCategoria[p.getCategoria() - 1]++;
            }
            for (String cambio : p.getHistorialCambios()) {
                if (cambio.contains("Dando prioridad a paciente por tiempo de espera excedido")) {
                    recategorizados++;
                    break;
                }
            }
        } 
        System.out.println("=Reporte Final=");
        System.out.println("Total de pacientes atendidos: " + totalPacientes);
        System.out.println("Tiempo promedio de espera: " + (sumaTiempoEspera / totalPacientes) + " minutos");
        System.out.println("Pacientes por categoría:");
        for (int i = 0; i < pacientesPorCategoria.length; i++) {
            System.out.println("Categoría C" + (i + 1) + ": " + pacientesPorCategoria[i]);
        }
        System.out.println("Pacientes recategorizados por tiempo de espera excedido: " + recategorizados);
        System.out.println( "================================\n");
    }
    }

    public static void main(String[] args) {
        SimuladorUrgencia simulador = new SimuladorUrgencia();

        simulador.simular(100); 
    }
}
