/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fog.test.perfeval;

import java.sql.DriverManager;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import static javax.swing.UIManager.get;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.placement.Controller;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.test.perfeval.criaJSON;
import org.fog.test.perfeval.SIoTSimulador;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

/**
 *
 * @author foletto
 */
public class Management extends Controller{
    
    //public int statusfog;
    public int troca;
    
    protected List<Management> fogbusy = new ArrayList<Management>();
    protected List<FogDevice> fog = new ArrayList<FogDevice>();
    protected List<FogDevice> cloud = new ArrayList<FogDevice>();
    
    private List<FogDevice> fogDevices;
    public List<Actuator> actuators;
    public List<Sensor> sensors;
    public List<Pe> peList;
    public List<Host> hostList;
    
    SIoTSimulador simulation = new SIoTSimulador();  
    private String nome;
    private double mips;
    private double ram;
    private double cpu;
    private double latencia;
    private int idmonitoramento;
    private int testeram;
    private int testeidlantencia;
    
	private Connection conn;
    
    
    
  
    public Management(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators) {
        super(name, fogDevices, sensors, actuators);
    }
    
    
    public FogDevice DivFogCloud()                                                                                                  {
        System.out.println("-----------------------------------------------------------");
        
        //Iterator itfogDevices = fogDevices.iterator();
        for(int j = 0; j < fogDevices.size();j++){
            if(fogDevices.get(j).getLevel() == 1){
                fog.add(fogDevices.get(j));
            }
            if(fogDevices.get(j).getLevel() == 0){
                cloud.add(fogDevices.get(j));
            }
        }
        
        System.out.println("Lista de Cloud:");
        for(FogDevice d : cloud){
            System.out.println(d.getName());
        }   

        System.out.println("\nLista de Fog:");
        for(FogDevice j : fog){                    
            System.out.println(j.getName());
        } 
        
        
    return null;
    }
        
    public String statusfog() throws SQLException{
        //Iterator ifog = fog.iterator();
        System.out.println("\nSTATUS DA FOG:");
        
        java.sql.Connection conexao = org.fog.test.perfeval.ConexaoMySQL.getConexaoMySQL();
        try {
        	int flag = 0;
        	for(int i = 0; i < fog.size();i++){
                if(flag == 0) {
		    		System.out.println("NodeName: "+fog.get(i).getNodeName());
		            System.out.println("ID: "+fog.get(i).getId());
		            nome  = fog.get(i).getNodeName();
		            idmonitoramento = fog.get(i).getId();
		            System.out.println("UtilizationHostMips: " + fog.get(i).getHost().getUtilizationMips());
		            double mipstotal = fog.get(i).getHost().getTotalMips();
		            double mipsatual = fog.get(i).getRatePerMips();
		            double mips = mipstotal - mipsatual;
		            
		            System.out.println("UtilizationHostCpu: " + fog.get(i).getHost().getUtilizationOfCpu());
		            cpu = fog.get(i).getHost().getUtilizationOfCpu();
		            System.out.println("UtilizationHostRam: " + fog.get(i).getHost().getUtilizationOfRam()); 
		            ram =  fog.get(i).getHost().getUtilizationOfRam();
		            System.out.println("UtilizationHostBw: " + fog.get(i).getHost().getUtilizationOfBw()); 
		            latencia = fog.get(i).getHost().getUtilizationOfBw();
		            
		            testeram =  0;
		            testeidlantencia =  0;
		            System.out.println("\n");
		            
		            String sql = "INSERT INTO monitoramento (cpu, mips, idLatencia, monitoramentocol) VALUES(?, ?, ?, ?)";
		            
		            //PreparedStatement grava = (PreparedStatement) Connection.createStatement(); 
		            PreparedStatement grava = (PreparedStatement) conexao.prepareStatement(sql);
		            System.out.println("aquiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
		
		            grava.setDouble(1, cpu);
		            //System.out.println("2");
		            grava.setDouble(2, mips);
		            //System.out.println("3");
		            grava.setDouble(3, latencia);
		            //System.out.println("4");
		            grava.setString(4, nome);
		                            
		            grava.executeUpdate();
		            System.out.println("Executado!");
                }else if(flag != 0) {
                	System.out.println("NodeName: "+fog.get(i).getNodeName());
		            System.out.println("ID: "+fog.get(i).getId());
		            nome  = fog.get(i).getNodeName();
		            idmonitoramento = fog.get(i).getId();
		            System.out.println("UtilizationHostMips: " + fog.get(i).getHost().getUtilizationMips());
		            double mipstotal = fog.get(i).getHost().getTotalMips();
		            double mipsatual = fog.get(i).getRatePerMips();
		            double mips = mipstotal - mipsatual;
		            
		            System.out.println("UtilizationHostCpu: " + fog.get(i).getHost().getUtilizationOfCpu());
		            cpu = fog.get(i).getHost().getUtilizationOfCpu();
		            System.out.println("UtilizationHostRam: " + fog.get(i).getHost().getUtilizationOfRam()); 
		            ram =  fog.get(i).getHost().getUtilizationOfRam();
		            System.out.println("UtilizationHostBw: " + fog.get(i).getHost().getUtilizationOfBw()); 
		            latencia = fog.get(i).getHost().getUtilizationOfBw();
		            
		            testeram =  0;
		            testeidlantencia =  0;
		            System.out.println("\n");
		            
		            String sql = "UPDATE monitoramento SET cpu = ?, mips = ? where monitoramentocol";
		            
		            //PreparedStatement grava = (PreparedStatement) Connection.createStatement(); 
		            PreparedStatement grava = (PreparedStatement) conexao.prepareStatement(sql);
		            System.out.println("aquiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
		
		            grava.setDouble(1, cpu);
		            //System.out.println("2");
		            grava.setDouble(2, mips);
		            //System.out.println("3");
		            grava.setDouble(3, latencia);
		            //System.out.println("4");
		            grava.setString(4, nome);
		                            
		            grava.executeUpdate();
		            System.out.println("Executado!");
                	
                }
                /*
                try {
    				Thread.sleep(5000);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}*/
            }
            
            conexao.close();
            System.out.println("Conex�o close!");
        } catch (SQLException e) {
        	System.out.println(e);
        } finally {
        	conexao.close();
        }
        
        
        for(int i=0; i < fogDevices.size(); i++){
            System.out.println("FOG DEVICES %s" + i);
            System.out.println("Fog:" + this.fogDevices.get(i).getName());
        }
        
        return null;
    }
    
    @SuppressWarnings("unused")
	private void getConexaoMySQL() {
		
	}


	//Aqui chama o metodo criaTopologia da classe criaTopo para criar o JSON
    public void criaTopo(){
        criaJSON teste = new criaJSON(fog, cloud, sensors);
        teste.criaTopologia(fog, cloud, sensors);
    }
    
    
    public String statuscloud(){
        //Iterator icloud = cloud.iterator();
        System.out.println("\nSTATUS DA CLOUD:");
        for(int i = 0; i < cloud.size();i++){
            System.out.println("NodeName: "+fog.get(i).getNodeName());
            System.out.println("ID: "+fog.get(i).getId());
            System.out.println("getMips: "+fog.get(i).getMips());
            System.out.println("UtilizationHostMips: " + fog.get(i).getHost().getUtilizationMips());
            System.out.println("UtilizationHostCpu: " + fog.get(i).getHost().getUtilizationOfCpu());      
            System.out.println("UtilizationHostCpu: " + fog.get(i).getHost().getUtilizationOfRam());                          
            System.out.println("\n");
        }        
        
        return null;
    }
    
    private FogDevice getCloud(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().equals("cloud"))
				return dev;
		return null;
	}
    

    
    public List<FogDevice> getFogDevices() {
            return fogDevices;
    }

    public void setFogDevices(List<FogDevice> fogDevices) {
            this.fogDevices = fogDevices;
    }   
    
    public List<Sensor> getSensors() {
		return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
            for(Sensor sensor : sensors)
                    sensor.setControllerId(getId());
            this.sensors = sensors;
    }

    public List<Actuator> getActuators() {
            return actuators;
    }

    public void setActuators(List<Actuator> actuators) {
            this.actuators = actuators;
    }

    
    public void setFog(List<FogDevice>fog){
        this.fog = fog;
    }
    
    public List<FogDevice> getFog(){
        return fog;
    }

}
