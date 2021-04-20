/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.subliminalrich.editor;

import it.sauronsoftware.jave.AudioAttributes;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;

/**
 *
 * @author JasusRich
 */
public class Conversor {

    private String archivoConvertir;
    private String archivoConvertido;
    private int canales = 2;
    private int frecuencia = 48000;
    private int resolucion = 24;
    String errorMensaje = "";

    public Conversor(String audio, String convertido) {
        archivoConvertir = audio;
        archivoConvertido = convertido;
    }

    public Conversor(String audioEntrada) {
        this.archivoConvertir = audioEntrada;
    }
    public Conversor(){
        
    }

    public void setCanales(int canales) {
        this.canales = canales;
    }

    public void setSampleRate(int frecuencia) {
        this.frecuencia = frecuencia;
    }

    public void setResolucion(int resolucion) {
        this.resolucion = resolucion;
    }

    public String getArchivoConvertido() {
        return this.archivoConvertido;
    }

    // 0 = no neceista conversión, 1 = si necesita conversión, 2 = error
    public int necesitaConversion() throws IOException {
        AudioInputStream inFileStream = null;
        try {
            //Captura los formatos que tenemos y los que queremos
            File inFile = new File(archivoConvertir);
            AudioFormat inDataFormat;
            AudioFormat outDataFormat;
            inFileStream = AudioSystem.getAudioInputStream(inFile);
            inDataFormat = inFileStream.getFormat();
            outDataFormat = getAudioFormat();
            //inFileStream.close();
            //Comprobamos si necesitamos realizar una conversión
            if (inDataFormat.equals(outDataFormat)) {
                return 0;
            } else {
                return 1;
            }
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(Conversor.class.getName()).log(Level.SEVERE, null, ex);
            errorMensaje = "Audio no soportado para la conversion";
            return 2;
        } catch (IOException ex) {
            Logger.getLogger(Conversor.class.getName()).log(Level.SEVERE, null, ex);
            errorMensaje = "Ha fallado la lectura o escritura";
            return 2;
        } finally {
            if (inFileStream != null) {
                inFileStream.close();
            }
        }
    }

    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = this.frecuencia;
        int sampleSizeInBits = this.resolucion;
        int channels = this.canales;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }

    //Realiza el propio proceso de conversión.
    public void convertir() throws IOException {

        AudioFileFormat inFileFormat;
        File inFile;
        File outFile;

        //Abre los archivos de entrada y salida
        try {
            inFile = new File(archivoConvertir);
            outFile = new File(archivoConvertido);
        } catch (NullPointerException ex) {
            System.out.println("Error: una de las rutas de archivo " + ex + " no es correcta!");
            errorMensaje = "Una de las rutas de archivo no es correcta";
            return;
        }

        //Realiza la conversión
        AudioInputStream outWav = null;
        AudioInputStream inFileStream = null;
        try {

            // Cual es el formato de ARCHIVO de la entrada
            inFileFormat = AudioSystem.getAudioFileFormat(inFile);
            // Crea un stream con la entrada para trabajar con él
            inFileStream = AudioSystem.getAudioInputStream(inFile);
            // Comprueba el formato de DATOS del stream de entrada
            AudioFormat inDataFormat = inFileStream.getFormat();
            // Crea el formato del audio de salida llamando a la función creada más arriba
            AudioFormat outDataFormat = getAudioFormat();


            if (inFileFormat.getType() == AudioFileFormat.Type.WAVE && AudioSystem.isConversionSupported(outDataFormat, inDataFormat)) {
                // inFile is WAVE and can be converted to desired format, so let's work on it.

                outWav = AudioSystem.getAudioInputStream(outDataFormat, inFileStream);
                AudioSystem.write(outWav, AudioFileFormat.Type.WAVE, outFile);

                // Cerramos los streams para que no consuman recursos
                inFileStream.close();
                outWav.close();
            } else {
                System.out.println("Archivo de entrada " + inFile.getPath() + " no es WAV o no es posible convertirlo a la calidad adecuada.");
                errorMensaje = "La codificación del archivo no es compatible.";
                inFileStream.close();
                System.out.println("No se ha podido convertir el archivo\n");
            }
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Error: " + e + " no es un tipo de archivo soportado!");
            errorMensaje = "No es un tipo de archivo soportado.";
        } catch (IOException e) {
            System.out.println("Error: fallo al leer/escribir " + e + "!");
            errorMensaje = "Error al leer o escribir.";
        } finally {
            if (outWav != null) {
                outWav.close();
            }
            if (inFileStream != null) {
                inFileStream.close();
            }
        }
    }

    public void convertToMp3(String origen, String destino) throws IllegalArgumentException, InputFormatException, EncoderException {
        File source = new File(origen);
        File target = new File(destino);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(new Integer(128000));
        audio.setChannels(new Integer(2));
        audio.setSamplingRate(new Integer(48000));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(source, target, attrs);
    }
    
    public void convertToWav(String origen, String destino) throws IllegalArgumentException, InputFormatException, EncoderException {
        File source = new File(origen);
        File target = new File(destino);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setBitRate(new Integer(128000));
        audio.setChannels(new Integer(2));
        audio.setSamplingRate(new Integer(48000));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(source, target, attrs);
    }
}
