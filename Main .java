import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class Paciente implements Comparable<Paciente>{
    private String nombre, apellido, id, estado, area;
    private int categoria;
    private long tiempoLlegada;
    private long tiempoAtencion= -1;
    private Stack<String> historialCambios;

    public Paciente(String nombre, String apellido, String id, int categoria, long tiempoLlegada, String area){
        this.nombre=nombre;
        this.apellido=apellido;
        this.id=id;
        this.categoria=categoria;
        this.tiempoLlegada=tiempoLlegada;
        this.estado="en_espera";
        this.area=area;
        this.historialCambios = new Stack<>();
    }
    public String getNombre(){
        return nombre;
    }
    public String getApellido(){
        return apellido;
    }
    public long tiempoEsperaActual(long tiempoActual){
        return (tiempoActual - tiempoLlegada)/60;
    }
    public void registrarCambio(String descripcion){
        historialCambios.push(descripcion);
    }
    public String obtenerUltimoCambio() {
        return historialCambios.isEmpty() ? null : historialCambios.pop();
    }
    public int getCategoria(){
        return categoria;
    }
    public String getId(){
        return id;
    }
    public long getTiempoLlegada(){
        return tiempoLlegada;
    }
    public void setCategoria(int cat){
        this.categoria=cat;
    }
    public void setEstado(String estado){
        this.estado=estado;
    }
    public String getEstado(){
        return estado;
    }
    public String getArea(){
        return area;
    }
    public long getTiempoAtencion(){
        return tiempoAtencion;
    }
    public void setTiempoAtencion(long tiempo){
        this.tiempoAtencion=tiempo;
    }
    public String getNombreCompleto(){
        return nombre + " " + apellido;
    }
    private String convertirAStrHora(long segundosDesdeInicio){
        LocalTime base= LocalTime.MIDNIGHT;
        LocalTime hora= base.plusSeconds(segundosDesdeInicio);
        return hora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    public String resumen(){
        String llegadaStr= convertirAStrHora(tiempoLlegada);
        if(tiempoAtencion<0){
            return String.format("[%s] %s | C%d | %s | Llegó: %s hrs | Atendido: No atendido", id, getNombreCompleto(), categoria, area, llegadaStr);
        }
        String atencionStr= convertirAStrHora(tiempoAtencion*60);
        long esperaSeg= (tiempoAtencion*60) - tiempoLlegada;
        String esperaStr= String.format("%02d:%02d:%02d", esperaSeg/3600, (esperaSeg%3600)/60, esperaSeg%60);
        return String.format("[%s] %s | C%d | %s | Llegó: %s hrs | Atendido: %s hrs | Espera de: %s hrs", id, getNombreCompleto(), categoria, area, llegadaStr, atencionStr, esperaStr);
    }

    @Override
    public int compareTo(Paciente otro){
        if(this.categoria!=otro.categoria){
            return Integer.compare(this.categoria, otro.categoria);
        }
        return Long.compare(this.tiempoLlegada, otro.tiempoLlegada);
    }
}

class AreaAtencion{
    private String nombre;
    private PriorityQueue<Paciente> pacientesHeap;
    private int capacidadMaxima;

    public AreaAtencion(String nombre, int capacidadMaxima){
        this.nombre=nombre;
        this.capacidadMaxima=capacidadMaxima;
        this.pacientesHeap = new PriorityQueue<>();
    }
    public void ingresarPaciente(Paciente p){
        if(!estaSaturada()){
            pacientesHeap.add(p);
        }
    }
    public Paciente atenderPaciente(){
        return pacientesHeap.poll();
    }
    public boolean estaSaturada(){
        return pacientesHeap.size()>=capacidadMaxima;
    }
    public List<Paciente> obtenerPacientesPorHeapSort(){
        List<Paciente> lista = new ArrayList<>(pacientesHeap);
        lista.sort(null);
        return lista;
    }
}

class Hospital{
    private Map<String, Paciente> pacientesTotales = new HashMap<>();
    private PriorityQueue<Paciente> colaAtencion = new PriorityQueue<>();
    private Map<String, AreaAtencion> areasAtencion = new HashMap<>();
    private List<Paciente> pacientesAtendidos = new ArrayList<>();
    private List<Paciente> pacientesNoAtendidos = new ArrayList<>();

    public Hospital(int capacidadMax){
        areasAtencion.put("SAPU", new AreaAtencion("SAPU", capacidadMax));
        areasAtencion.put("urgencia_adulto", new AreaAtencion("urgencia_adulto", capacidadMax));
        areasAtencion.put("infantil", new AreaAtencion("infantil", capacidadMax));
    }
    public void registrarPaciente(Paciente p){
        pacientesTotales.put(p.getId(), p);
        colaAtencion.add(p);
        System.out.println("[LLEGADA] " + p.resumen());
    }
    public void reasignarCategoria(String id, int nuevaCategoria){
        Paciente p= pacientesTotales.get(id);
        if(p!=null){
            p.registrarCambio("Reasignado de C" + p.getCategoria() + " a C" + nuevaCategoria);
            p.setCategoria(nuevaCategoria);
        }
    }
    public Paciente atenderSiguiente(long minutoActual){
        Paciente p= colaAtencion.poll();
        if(p!=null){
            p.setEstado("atendido");
            p.setTiempoAtencion(minutoActual);
            pacientesAtendidos.add(p);
            areasAtencion.get(p.getArea()).ingresarPaciente(p);
            System.out.println("[ATENCION] " + p.resumen());
        }
        return p;
    }
    public void registrarNoAtendidos(){
        for(Paciente p : colaAtencion){
            pacientesNoAtendidos.add(p);
        }
    }
    public List<Paciente> getPacientesAtendidos(){
        return pacientesAtendidos;
    }
    public List<Paciente> getPacientesNoAtendidos(){
        return pacientesNoAtendidos;
    }
    public int getTotalPacientes(){
        return pacientesTotales.size();
    }
    public boolean existePaciente(String id){
        return pacientesTotales.containsKey(id);
    }
    public Paciente getPacientePorId(String id){
        return pacientesTotales.get(id);
    }
}

class GeneradorPacientes{
    private static final String[] nombres= {"Arturo", "Eugenia", "Clotilde", "Basilio", "Remigio", "Eusebia", "Jacinto", "Evaristo", "Hortensia", "Prudencio", "Inés", "Filomena", "Serapio", "Teófila", "Pascual", "Ciriaco", "Dolores", "Hilario", "Justa", "Aniceto"};
    private static final String[] apellidos= {"Maldonado", "Cifuentes", "Barriga", "Godoy", "Albornoz", "Rebolledo", "Aránguiz", "Alcayaga", "Mondaca", "Villagrán", "Urrutia", "Pizarro", "Labbé", "Olivares", "Zamorano", "Alarcón", "Araya", "Cordero", "Sepúlveda", "Aguayo"};
    private static final String[] areas= {"SAPU", "urgencia_adulto", "infantil"};

    public static List<Paciente> generarPacientes(int N, int intervaloSegundos){
        List<Paciente> lista = new ArrayList<>();
        long base=0;
        for (int i=0; i<N; i++){
            String nombre= nombres[(int)(Math.random()*nombres.length)];
            String apellido= apellidos[(int)(Math.random()*apellidos.length)];
            String id= "P" + (1000+i);
            int cat= generarCategoria();
            long llegada= base+i*intervaloSegundos;
            String area= areas[i%areas.length];
            lista.add(new Paciente(nombre, apellido, id, cat, llegada, area));
        }
        return lista;
    }
    public static int generarCategoria(){
        int r=(int)(Math.random()*100); //al ser if que retornan, se calcula teniendo en cuenta el numero anterior como el 0 o inicio de la probabilidad
        if(r<10) return 1;
        if(r<25) return 2; //ejemplo r, tiene que 10<r<25, entre el 10% y 25% hay 15%.
        if(r<43) return 3;
        if(r<70) return 4;
        return 5; //no cuenta con if ya que solo ocurre si: 70<r<100, dando el 30% faltante para C5
    }
}

class SimuladorUrgencia{
    private Hospital hospital;

    public SimuladorUrgencia(int capacidadMax){
        this.hospital = new Hospital(capacidadMax);
    }
    public void simular(int pacientesPorDia){
        int intervaloIngreso= 1440/pacientesPorDia;
        int intervaloSegundos= intervaloIngreso*60;
        List<Paciente> pacientes= GeneradorPacientes.generarPacientes(pacientesPorDia, intervaloSegundos);
        Queue<Paciente> colaLlegada = new LinkedList<>(pacientes);
        for(int minuto=0; minuto<1440; minuto++){
            if((minuto%intervaloIngreso)==0 && !colaLlegada.isEmpty()){
                hospital.registrarPaciente(colaLlegada.poll());
            }
            if((minuto%15)==0){
                hospital.atenderSiguiente(minuto);
            }
        }
        hospital.registrarNoAtendidos();
        System.out.println("\n[Cierre Del Hospital]" +
                            "\nPacientes Totales: " + hospital.getTotalPacientes() +
                            "\nPacientes Atendidos: " + hospital.getPacientesAtendidos().size() +
                            "\nPacientes No Atendidos: " + hospital.getPacientesNoAtendidos().size());
        mostrarMenu(hospital);
    }
    private void crearArchivoConPacientes(String nombreArchivo){
        List<Paciente> pacientes = GeneradorPacientes.generarPacientes(100, 600);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))){
            for(Paciente p : pacientes){
                writer.write(p.getNombre() + "," + p.getApellido() + "," + p.getArea());
                writer.newLine();
            }
            System.out.println("Archivo creado con éxito: " + nombreArchivo);
        }catch(IOException e){
            System.out.println("Error al crear el archivo: " + e.getMessage());
        }
    }
    public void simularDesdeArchivo(String nombreArchivo){
        System.out.println(">> Cargando pacientes desde archivo...");
        File archivo = new File(nombreArchivo);
        if(!archivo.exists()){
            System.out.println("El archivo no existe. Creando un nuevo archivo con pacientes...");
            crearArchivoConPacientes(nombreArchivo);
        }
        List<Paciente> pacientes = new ArrayList<>();
        List<String> areasValidas = Arrays.asList("SAPU", "urgencia_adulto", "infantil");
        long base=0;
        try(BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))){
            String linea;
            int i=0;
            while((linea=br.readLine())!=null){
                String[] partes=linea.split(",");
                if(partes.length<3){
                    continue;
                }
                String nombre= partes[0].trim();
                String apellido= partes[1].trim();
                String area= partes[2].trim();
                if(!areasValidas.contains(area)){
                    continue;
                }
                int cat= GeneradorPacientes.generarCategoria();
                String id= "P" + (1000 + i);
                long llegada= base+i*600;
                Paciente p = new Paciente(nombre, apellido, id, cat, llegada, area);
                pacientes.add(p);
                i++;
            }
        }
        catch(IOException e){
            System.out.println("Error al leer archivo: " + e.getMessage());
            return;
        }
        Queue<Paciente> colaLlegada = new LinkedList<>(pacientes);
        Random rand = new Random();
        int minutoError= rand.nextInt(1200);
        String pacienteErrorID=null;
        int contadorAtencionPostError=0;
        boolean errorDetectado=false;
        for(int minuto=0; minuto<1440; minuto++){
            if((minuto%10)==0 && !colaLlegada.isEmpty()){
                Paciente nuevo=colaLlegada.poll();
                hospital.registrarPaciente(nuevo);
                if(minuto>=minutoError && pacienteErrorID==null){
                    pacienteErrorID=nuevo.getId();
                    System.out.println("[ERROR] Paciente " + nuevo.getNombreCompleto() + " categorizado incorrectamente en C" + nuevo.getCategoria());
                }
            }
            if((minuto%15)==0){
                Paciente atendido=hospital.atenderSiguiente(minuto);
                if(pacienteErrorID!=null && errorDetectado==false){
                    if(atendido!=null && !atendido.getId().equals(pacienteErrorID)){
                        contadorAtencionPostError++;
                        if(contadorAtencionPostError == 3+rand.nextInt(3)){
                            Paciente pacienteErroneo=hospital.getPacientePorId(pacienteErrorID);
                            if(pacienteErroneo!=null){
                                int nuevaCat;
                                do{
                                    nuevaCat= 1+rand.nextInt(pacienteErroneo.getCategoria()-1);
                                }while(nuevaCat>=pacienteErroneo.getCategoria());
                                hospital.reasignarCategoria(pacienteErrorID, nuevaCat);
                                System.out.println("[CORRECCION] Reasignada categoría del paciente " + pacienteErroneo.getNombreCompleto() + " a C" + nuevaCat);
                                errorDetectado=true;
                            }
                        }
                    }
                }
            }
        }
        hospital.registrarNoAtendidos();
        System.out.println("\n[Cierre Del Hospital]" +
                "\nPacientes Totales: " + hospital.getTotalPacientes() +
                "\nPacientes Atendidos: " + hospital.getPacientesAtendidos().size() +
                "\nPacientes No Atendidos: " + hospital.getPacientesNoAtendidos().size());
        mostrarMenu(hospital);
    }
    private void mostrarMenu(Hospital hospital){
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.print("\n-| MENÚ DE RESULTADOS |-" +
                            "\n1. Ver pacientes atendidos" +
                            "\n2. Ver pacientes no atendidos" +
                            "\n3. Total atendidos por área" +
                            "\n4. Total atendidos por categoría" +
                            "\n5. Tiempos de atención por paciente" +
                            "\n6. Promedio de espera por categoría" +
                            "\n7. Lista de pacientes que excedieron el tiempo máximo de espera" +
                            "\n8. Reasignar categoría de un paciente" +
                            "\n0. Salir" +
                            "\nSeleccione una opción: ");
            int opcion=sc.nextInt();
            switch(opcion){
                case 1 -> hospital.getPacientesAtendidos().forEach(p -> System.out.println(p.resumen()));
                case 2 -> hospital.getPacientesNoAtendidos().forEach(p -> System.out.println(p.resumen()));
                case 3 -> {
                    Map<String, Integer> conteo = new HashMap<>();
                    for(Paciente p : hospital.getPacientesAtendidos()){
                        conteo.put(p.getArea(), conteo.getOrDefault(p.getArea(), 0) + 1);
                    }
                    conteo.forEach((area, count) -> System.out.println(area + ": " + count));
                }
                case 4 -> {
                    Map<Integer, Integer> conteo = new HashMap<>();
                    for(Paciente p : hospital.getPacientesAtendidos()){
                        conteo.put(p.getCategoria(), conteo.getOrDefault(p.getCategoria(), 0) + 1);
                    }
                    conteo.forEach((cat, count) -> System.out.println("C" + cat + ": " + count));
                }
                case 5 -> {
                    for(Paciente p : hospital.getPacientesAtendidos()){
                        if(p.getTiempoAtencion()>=0){
                            long esperaMin= (p.getTiempoAtencion()*60 - p.getTiempoLlegada())/60;
                            System.out.println(p.getNombreCompleto() + " fue atendido a los " + esperaMin + " minutos");
                        }
                    }
                }
                case 6 -> {
                    Map<Integer, List<Long>> esperas = new HashMap<>();
                    for(Paciente p : hospital.getPacientesAtendidos()){
                        if(p.getTiempoAtencion()>=0){
                            long espera = p.getTiempoAtencion() - (p.getTiempoLlegada()/60);
                            esperas.computeIfAbsent(p.getCategoria(), k -> new ArrayList<>()).add(espera);
                        }
                    }
                    esperas.forEach((cat, lista) -> {
                        long suma= lista.stream().mapToLong(Long::longValue).sum();
                        System.out.println("Categoría C" + cat + ": Promedio = " + (suma/lista.size()) + " min");
                    });
                }
                case 7 -> {
                    Map<Integer, Integer> maximos = Map.of(1, 15, 2, 30, 3, 60, 4, 180, 5, 360);
                    for(Paciente p : hospital.getPacientesAtendidos()){
                        if(p.getTiempoAtencion()>=0){
                            long espera= p.getTiempoAtencion() - (p.getTiempoLlegada()/60);
                            int max= maximos.getOrDefault(p.getCategoria(), 9999);
                            if(espera>max){
                                System.out.println(p.getNombreCompleto() + " (Categoría C" + p.getCategoria() + ") esperó " + espera + " min (máx: " + max + ")");
                            }
                        }
                    }
                }
                case 8 -> {
                    System.out.print("Ingrese ID del paciente: ");
                    String id=sc.next();
                    if(!hospital.existePaciente(id)){
                        System.out.println("No se encontró un paciente con ese ID.");
                    }
                    else{
                        System.out.print("Ingrese nueva categoría (1 a 5): ");
                        int nuevaCategoria=sc.nextInt();
                        if(nuevaCategoria>=1 && nuevaCategoria<=5){
                            hospital.reasignarCategoria(id, nuevaCategoria);
                            System.out.println("Categoría actualizada correctamente.");
                            System.out.println(hospital.getPacientePorId(id).resumen());
                        }
                        else{
                            System.out.println("Categoría inválida.");
                        }
                    }
                }
                case 0 -> {
                    System.out.println("\n-[Saliendo del menú...]-\n");
                    return;
                }
                default -> System.out.println("\n---[Opción inválida.]---\n");
            }
        }
    }
}

public class Main{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        boolean bool=true;
        while(bool){
            System.out.print("\n-| MENÚ PRINCIPAL |-" +
                "\n1) Iniciar simulación con lista aleatoria" +
                "\n2) Iniciar simulación con lista guardada" +
                "\n3) Iniciar simulación saturada" +
                "\n0) Cerrar el programa" +
                "\nSeleccione una opción: ");
            int x=sc.nextInt();
            switch (x) {
                case 1 -> new SimuladorUrgencia(100).simular(144);
                case 2 -> new SimuladorUrgencia(100).simularDesdeArchivo("Pacientes_24h.txt");
                case 3 -> new SimuladorUrgencia(100).simular(200);
                default -> {
                    System.out.println(">> Cerrando programa...");
                    bool = false;
                }
            }
        }
        sc.close();
    }
}