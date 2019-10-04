package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.cloudbus.cloudsim.Vm;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import static org.fog.test.perfeval.SIoTSimulador.fogDevices;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.NormalDistribution;
import org.fog.test.perfeval.Management;
//https://www.researchgate.net/post/How_can_I_implement_a_new_scheduling_algorithm_in_ifogsim

public class SIoTSimulador {

    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static int numOfDepts = 4;

    //static double INFRARED_TRANSMISSION_TIME = 5.1;

    public static void main(String[] args) {
//        Management m = new Management();
        int i = 0;
        SIoTSimulador s = new SIoTSimulador();
        s.StarSIoTSimulador();
        
//        for(i=0; i<=2; i++){
//            s.StarSIoTSimulador();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        
//        System.exit(0);
	

    }
    
    public void StarSIoTSimulador(){
        Log.printLine("Starting SIotSimulator...");
        try {
            
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "siot_simulate"; // identifier of the application

            FogBroker broker = new FogBroker("broker");
            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            createFogDevices(broker.getId(), appId);

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

            moduleMapping.addModuleToDevice("cloud-module", "cloud"); // fixing all instances of the Connector module to the Cloud
            moduleMapping.addModuleToDevice("fog-siot-module", "fog-siot"); // fixing all instances of the Connector module to the Cloud
            Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);

            controller.submitApplication(application, 0, (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());          
            
            
            CloudSim.startSimulation();
            //controller.
            CloudSim.stopSimulation();

            
            
            Log.printLine("SIotSimulator finished!");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Ops, erros acontecem!!!");
        }
    }

    private static FogDevice addGw(String id, int userId, String appId, int parentId) {
        FogDevice dept = createFogDevice("d-" + id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
        fogDevices.add(dept);
        dept.setParentId(parentId);
        dept.setUplinkLatency(4); // latency of connection between gateways and proxy server is 4 ms		
        return dept;
    }

    /**
     * Creates the fog devices in the physical topology of the simulation.
     *
     * @param userId
     * @param appId
     */
    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
        cloud.setParentId(-1);
        cloud.setUplinkLatency(235.732);
        cloud.setDownlinkBandwidth(235.732);
        //cloud.
                
        FogDevice fog = createFogDevice("fog-siot", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
        fog.setUplinkLatency(235.732);
        fog.setDownlinkBandwidth(235.732);
        fog.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server       

        FogDevice fog2 = createFogDevice("fog-siot2", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
        fog2.setUplinkLatency(235.732);
        fog2.setDownlinkBandwidth(235.732);
        fog2.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server       
        
        fogDevices.add(cloud);
        fogDevices.add(fog);
        fogDevices.add(fog2);
        addSensors(fog, userId, appId);

//        for (int i = 0; i < numOfDepts; i++) {
//            addGw(i + "", userId, appId, fog.getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
//        }
    }

    public static FogDevice createFogDevice(String nodeName, int mips,
            int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );
               
        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);
        

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.disable();
        HostDynamicWorkload h = fogdevice.getHost();
        System.out.println( "CPU do host " + h.getId() + " = "+ h.getUtilizationOfCpu() );
        fogdevice.setLevel(level);

//        @TODO: Fernando: Métodos adicionados
//        fogdevice.setRam(ram);
//        fogdevice.setNodeName(nodeName);
//        fogdevice.setMips(mips); //<--- ADD ESTA LINHA NA SIoTSimulador no fim do metodo CreateFogDevice
        return fogdevice;
    }

    private static void addSensors(FogDevice fog, int userId, String appId) {
        /*
         Mais importante, define as características de saída do sensor e a 
         distribuição da intertransmissão da tupla, 
         que identifica a taxa de chegada da tupla no gateway.
         Ao definir valores apropriados desses atributos, dispositivos como câmeras 
         inteligentes e carros conectados podem ser simulados
         */       
        final Sensor infraRed = new Sensor("infraRed", "fog-siot", userId, appId, new NormalDistribution(1, 1)); // inter-transmission time of EEG sensor follows a deterministic distribution
        infraRed.setGatewayDeviceId(fog.getId());
        infraRed.setLatency(54.462);  // Latência de comunicação entre o sensor e a Fog 54.462ms
        sensors.add(infraRed);
        final Sensor luminosidade = new Sensor("luminosidade", "fog-siot", userId, appId, new NormalDistribution(1, 1)); // inter-transmission time of EEG sensor follows a deterministic distribution
        luminosidade.setGatewayDeviceId(fog.getId());
        luminosidade.setLatency(54.462);  // Latência de comunicação entre o sensor e a Fog 54.462ms
        sensors.add(luminosidade);
        final Sensor arcondicionado = new Sensor("ar", "fog-siot", userId, appId, new NormalDistribution(1, 1)); // inter-transmission time of EEG sensor follows a deterministic distribution
        arcondicionado.setGatewayDeviceId(fog.getId());
        arcondicionado.setLatency(54.462);  // Latência de comunicação entre o sensor e a Fog 54.462ms
        sensors.add(arcondicionado);
        final Sensor biometria = new Sensor("biometria", "fog-siot", userId, appId, new NormalDistribution(1, 1)); // inter-transmission time of EEG sensor follows a deterministic distribution
        biometria.setGatewayDeviceId(fog.getId());
        biometria.setLatency(54.462);  // Latência de comunicação entre o sensor e a Fog 54.462ms     
        sensors.add(biometria);       
    }

    /**
     * Function to create the EEG Tractor Beam game application in the DDF
     * model.
     *
     * @param appId unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {
        /*
         http://repositorio.unicamp.br/bitstream/REPOSIP/324292/1/Lopes_MarcioMoraes_M.pdf
         https://social.stoa.usp.br/pedropaulovc/tcc/estudo-do-cloudsim
         */

        Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)

        application.addAppModule("cloud-module", 10); // adding module Client to the application model
        application.addAppModule("fog-siot-module", 10);
        /*
         tupleCpuLength -> CPU length (in MIPS) of tuples carried by the application edge
         tupleNwLength  -> Network length (in bytes) of tuples carried by the application edge
         */
        application.addAppEdge("infraRed", "fog-siot", 3000, 500, "infraRed", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("luminosidade", "fog-siot", 3000, 500, "luminosidade", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("ar", "fog-siot", 3000, 500, "ar", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("biometria", "fog-siot", 3000, 500, "biometria", Tuple.UP, AppEdge.SENSOR);
        
        /*
         Nessa parte teremos que simular todas as chamadas entre sensores fog e fog Cloud
         a quantidade e simulação serão definidas nos loops
         */
        //Envio dos dados da coleta de uma turma com as biometrias coletadas da FOG para a cloud
        application.addAppEdge("fog-siot", "cloud-module", 100, 900000, "envia-presencas", Tuple.UP, AppEdge.MODULE);
        //Cloud: envio dos dados para a FOG
        application.addAppEdge("fog-siot", "cloud-module", 20, 50, "recebe-turmas-biometrias", Tuple.DOWN, AppEdge.MODULE);

        //application.addTupleMapping("fog-siot", " infraRed ", "envia-presencas", new FractionalSelectivity(0.3));
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
            {
                add("infraRed");
                add("luminosidade");
                add("ar");
                add("biometria");
                //add("fog-siot");
            }
        });
        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {

            {
                for (int i = 0; i < 60; i++) {
                    add("biometria");
                }
                add("fog-siot");
            }
        });
        final AppLoop loop3 = new AppLoop(new ArrayList<String>() {
            {
                add("cloud");
            }
        });
        
        final AppLoop loop4 = new AppLoop(new ArrayList<String>() {
            {
               //Log.printLine(fogDevices.get(0));
               add("fog-siot");
            }
        });
        
        List<AppLoop> loops = new ArrayList<AppLoop>() {
            {
                add(loop1);
                add(loop2);
                add(loop3);
                add(loop4);
            }
        };
        application.setLoops(loops);
        
        Log.printLine("------------------------Hierarquia------------------------");
        Log.printLine("Cloud: " + loop3.getModules());
        Log.printLine("Fog: " + loop4.getModules());
        Log.printLine("\nSensores: ");
        for(int i = 0; i < loop1.getModules().size(); i++){
            Log.printLine(loop1.getModules().get(i));
        }        
        

        Log.printLine("----------------------------------------------------------");
        
        return application;
    
    }    
}
