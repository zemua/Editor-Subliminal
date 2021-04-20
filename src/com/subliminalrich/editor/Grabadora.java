package com.subliminalrich.editor;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JasusRich
 */
public class Grabadora {

    private int canales = 1;
    private int frecuencia = 44100;
    private int resolucion = 16;
    int error = 0;
    private String rutaGrabacion;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    String errorMensaje = "";
    
    /*public void setRuta(){
        // Propiedad del sistema, carpeta de archivos temporales
        String property = "java.io.tmpdir";
        // Pasar el directorio temporal del sistema a la variable
        String tempDir = System.getProperty(property);
        this.rutaGrabacion = tempDir + Long.toString(System.nanoTime()) + ".wav";
    }*/
    
    public void setRuta(File file){
        // Poner aquí la ruta completa desde el directorio de destino
        this.rutaGrabacion = file.getAbsolutePath() + System.getProperty("file.separator") + "Grabacion" + Long.toString(System.nanoTime()) + ".wav";
    }
    
    String getRuta(){
        return rutaGrabacion;
    }
    
    String getName(){
        return new File(rutaGrabacion).getName();
    }
    
    AudioFormat getAudioFormat() {
        float sampleRate = this.frecuencia;
        int sampleSizeInBits = this.resolucion;
        int channels = this.canales;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }
    
    // Aquí empieza el Snipet de la grabadora
    
    void detener(){
        targetDataLine.stop();
        targetDataLine.close();
    }
    
    void captureAudio(){
    try{
      //Get things set up for capture
      audioFormat = getAudioFormat();
      DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
      targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);

      //Create a thread to capture the microphone
      // data into an audio file and start the
      // thread running.  It will run until the
      // Stop button is clicked.  This method
      // will return after starting the thread.
      
      //new CaptureThread().start();

    }catch (Exception e) {
      e.printStackTrace();
      this.error = 1;
      return;
    }//end catch
  }//end captureAudio method
}
