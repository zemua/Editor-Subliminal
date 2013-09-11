/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.subliminalrich.editor;

import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author JasusRich
 */
public class ModulateWav {

    // Variables de rutas de archivos
    private String voz; // Este es el original, el archivo que abrimos
    private String vozConvertida;
    private String silent;
    private String musica;
    private String isocronico;
    private String isocronicoEnvelope;
    private String isocronicoIntegrado;
    private String binaural;
    private String vocoder;
    private String output; // Esta es la salida final
    private int frecuenciaMuestreo = 48000; // Samples per second
    private int frecuenciaModulacionSilent = 15000;
    int error = 0;
    String errorMensaje = "";
    static boolean errorStatic = false;
    private int repeticiones = 30;
    private int sobreModulacion = 0;
    private double porcentajeSobreModulacion = 0.0;

    public int getSobreModulacion() {
        return sobreModulacion;
    }

    public double getPorcentajeSobremodulacion() {
        return porcentajeSobreModulacion;
    }

    ModulateWav(String voz) {
        this.voz = voz;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    public int getRepeticiones() {
        return this.repeticiones;
    }

    public int getFrecuenciaModulacionSilent() {
        return frecuenciaModulacionSilent;
    }

    public void setIsocronicoEnvelope(String isocronicoEnvelope) {
        this.isocronicoEnvelope = isocronicoEnvelope;
    }

    public String getIsocronicoEnvelope() {
        return this.isocronicoEnvelope;
    }

    public void setFrecuenciaMuestreo(int frecuencia) {
        this.frecuenciaMuestreo = frecuencia;
    }

    public int getFrecuenciaMuestreo() {
        return this.frecuenciaMuestreo;
    }

    public void setVozEntrada(String entrada) {
        this.voz = entrada;
    }

    public void setVozConvertida(String salida) {
        this.vozConvertida = salida;
    }

    public String getVozConvertida() {
        return this.vozConvertida;
    }

    public void setSilent(String silent) {
        this.silent = silent;
    }

    public void SetModulacion(int frecuencia) {
        this.frecuenciaModulacionSilent = frecuencia;
    }

    public String getVozEntrada() {
        return this.voz;
    }

    public String getSilent() {
        return this.silent;
    }

    public void setMusica(String musica) {
        this.musica = musica;
    }

    public String getMusica() {
        return this.musica;
    }

    public void setIsocronico(String isocronico) {
        this.isocronico = isocronico;
    }

    public String getIsocronico() {
        return this.isocronico;
    }

    public void setisocronicoIntegrado(String isocronicoIntegrado) {
        this.isocronicoIntegrado = isocronicoIntegrado;
    }

    public String getisocronicoIntegrado() {
        return this.isocronicoIntegrado;
    }

    public void setbinaural(String binaural) {
        this.binaural = binaural;
    }

    public String getbinaural() {
        return this.binaural;
    }

    public void setVocoder(String vocoder) {
        this.vocoder = vocoder;
    }

    public String getVocoder() {
        return this.vocoder;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return this.output;
    }

    public void repetir(int repeticiones, String origen, String destino, JTextArea mensajes) throws IOException {
        WavFile entradaB = null;
        WavFile salida = null;
        WavFile entrada = null;
        try {
            // Abrimos el archivo de origen para calcular el numero de frames
            entradaB = WavFile.openWavFile(new File(origen));
            //Get the number of frames of the audio
            long numFrames = entradaB.getNumFrames();
            numFrames = numFrames * repeticiones;
            // Ya tenemos toda la informacion y cerramos el archivo
            entradaB.close();

            // Creamos el archivo que va a ser la pista repetida X veces
            salida = WavFile.newWavFile(new File(destino), 2, numFrames, 24, this.frecuenciaMuestreo);

            for (int i = 0; i < repeticiones; i++) {
                try {
                    // En cada bucle creamos un archivo para leerlo desde el principio
                    // Un rewind sería más cojonudo, pero no se como se hace en la clase "WavFile"
                    entrada = WavFile.openWavFile(new File(origen));

                    // Create a buffer of 100 frames for 2 channels
                    double[][] bufferRead = new double[2][100];

                    // Create a buffer of 100 frames and 2 chanels where we are going to WRITE
                    double[][] bufferWrite = new double[2][100];

                    // Initialise a local frame counter to make the sine wave
                    long frameCounter = 0;

                    //Contador del número de frames leídos hasta el momento en el bucle
                    int framesRead;

                    do {
                        // Read frames into buffer
                        framesRead = entrada.readFrames(bufferRead, 100);

                        // Loop through frames and create the silent buffer
                        for (int s = 0; s < framesRead; s++) {
                            bufferWrite[0][s] = bufferRead[0][s];
                            bufferWrite[1][s] = bufferRead[1][s];
                            frameCounter++;
                        }
                        // Write the frames readed to the silent buffer into the silent file
                        salida.writeFrames(bufferWrite, framesRead);
                    } while (framesRead != 0);

                    entrada.close();
                } catch (Exception e) {
                    System.out.println(e.getStackTrace());
                    error = 1;
                }
            }
            salida.close();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            error = 1;
        } finally {
            try {
                if (entradaB != null) {
                    entradaB.close();
                }
                if (entrada != null) {
                    entrada.close();
                }
                if (salida != null) {
                    salida.close();
                }
            } catch (Exception e) {
                System.out.println("No se han cerrado los streams tras repetir el archivo");
            }
        }
    }

    public void repetirHastaIgualar(String archivoRepetir, String archivoIgualar, String destino) {
        try {
            // Abrimos el archivo de origen para calcular el numero de frames
            WavFile entradaB = WavFile.openWavFile(new File(archivoRepetir));
            WavFile comparaB = WavFile.openWavFile(new File(archivoIgualar));
            //Get the number of frames of the audio
            long numFramesEntrada = entradaB.getNumFrames();
            long numFramesIgualar = comparaB.getNumFrames();
            double diferencia = java.lang.Math.ceil((double) numFramesIgualar / (double) numFramesEntrada);
            long totalFrames = numFramesEntrada * ((long) diferencia);
            //Crear la frecuencia de muestreo de la onda que va a modular la voz
            // Ya tenemos toda la informacion y cerramos el archivo
            entradaB.close();
            comparaB.close();
            if (getPequeno(archivoRepetir, archivoIgualar) == 1) {
                // Creamos el archivo que va a ser la pista repetida X veces
                WavFile salida = WavFile.newWavFile(new File(destino), 2, totalFrames, 24, frecuenciaMuestreo);

                for (int i = 0; i < diferencia; i++) {
                    // En cada bucle creamos un archivo para leerlo desde el principio
                    // Un rewind sería más cojonudo, pero no se como se hace en la clase "WavFile"
                    WavFile entrada = WavFile.openWavFile(new File(archivoRepetir));

                    // Create a buffer of 100 frames for 2 channels
                    double[][] bufferRead = new double[2][100];

                    // Create a buffer of 100 frames and 2 chanels where we are going to WRITE
                    double[][] bufferWrite = new double[2][100];

                    // Initialise a local frame counter to make the sine wave
                    long frameCounter = 0;

                    //Contador del número de frames leídos hasta el momento en el bucle
                    int framesRead;

                    do {
                        // Read frames into buffer
                        framesRead = entrada.readFrames(bufferRead, 100);

                        // Loop through frames and create the silent buffer
                        for (int s = 0; s < framesRead; s++) {
                            bufferWrite[0][s] = bufferRead[0][s];
                            bufferWrite[1][s] = bufferRead[1][s];
                            frameCounter++;
                        }
                        // Write the frames readed to the silent buffer into the silent file
                        salida.writeFrames(bufferWrite, framesRead);
                    } while (framesRead != 0);

                    entrada.close();
                }
                salida.close();
            } else {
                copiar(archivoRepetir, destino);
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            error = 1;
        }
    }

    public int getSegundos(String archivo) {
        // Los segundos son redondeados hacia arriba
        try {
            WavFile audio = WavFile.openWavFile(new File(archivo));
            long frames = audio.getNumFrames();
            long rate = audio.getSampleRate();
            int segundos = (int) java.lang.Math.ceil((double) frames / (double) rate);
            audio.close();
            return segundos;
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            error = 1;
            return 0;
        }
    }

    public int getPequeno(String archivo1, String archivo2) {
        try {
            WavFile audio1 = WavFile.openWavFile(new File(archivo1));
            long frames1 = audio1.getNumFrames();
            long rate1 = audio1.getSampleRate();
            double segundos1 = (double) frames1 / (double) rate1;
            audio1.close();

            WavFile audio2 = WavFile.openWavFile(new File(archivo2));
            long frames2 = audio2.getNumFrames();
            long rate2 = audio2.getSampleRate();
            double segundos2 = (double) frames2 / (double) rate2;
            audio2.close();

            if (segundos1 < segundos2) {
                return 1;
            } else if (segundos2 < segundos1) {
                return 2;
            } else {
                return 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 5;
        }
    }

    public void generarTonoEstereo(int samplerate, float segundos, String nombreArchivo, float frecuencia1, float frecuencia2) {
        try {
            int sampleRate = samplerate;    // Samples per second
            double duration = segundos;     // Seconds

            // Calculate the number of frames required for specified duration
            long numFrames = (long) (duration * sampleRate);

            // Create a wav file with the name specified as the first argument
            WavFile wavFile = WavFile.newWavFile(new File(nombreArchivo), 2, numFrames, 24, sampleRate);

            // Create a buffer of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the buffer size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the buffer, one tone per channel
                for (int s = 0; s < toWrite; s++, frameCounter++) {
                    buffer[0][s] = Math.sin(2.0 * Math.PI * frecuencia1 * frameCounter / sampleRate);
                    buffer[1][s] = Math.sin(2.0 * Math.PI * frecuencia2 * frameCounter / sampleRate);
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
            }

            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        }
    }

    public void generarPulsosEnveloped(int samplerate, float segundos, String nombreArchivo, float pulsos1, float pulsos2) {
        try {
            int sampleRate = samplerate;    // Samples per second
            double duration = segundos;     // Seconds

            // Calculate the number of frames required for specified duration
            long numFrames = (long) (duration * sampleRate);

            // Create a wav file with the name specified as the first argument
            WavFile wavFile = WavFile.newWavFile(new File(nombreArchivo), 2, numFrames, 24, sampleRate);

            // Create a buffer of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the buffer size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the buffer, one tone per channel
                for (int s = 0; s < toWrite; s++, frameCounter++) {
                    int t1;
                    int t2;
                    if (Math.sin(2.0 * Math.PI * pulsos1 * frameCounter / sampleRate) >= 0) {
                        t1 = 1;
                    } else {
                        t1 = 0;
                    }
                    if (Math.sin(2.0 * Math.PI * pulsos2 * frameCounter / sampleRate) >= 0) {
                        t2 = 1;
                    } else {
                        t2 = 0;
                    }
                    buffer[0][s] = t1;
                    buffer[1][s] = t2;
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
            }

            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        }
    }

    public void generarPulsos(int samplerate, float segundos, String nombreArchivo, float frecuencia1, float frecuencia2, float pulsos1, float pulsos2) {
        try {
            int sampleRate = samplerate;    // Samples per second
            double duration = segundos;     // Seconds

            // Calculate the number of frames required for specified duration
            long numFrames = (long) (duration * sampleRate);

            // Create a wav file with the name specified as the first argument
            WavFile wavFile = WavFile.newWavFile(new File(nombreArchivo), 2, numFrames, 24, sampleRate);

            // Create a buffer of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the buffer size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the buffer, one tone per channel
                for (int s = 0; s < toWrite; s++, frameCounter++) {
                    int t1;
                    int t2;
                    if (Math.sin(2.0 * Math.PI * pulsos1 * frameCounter / sampleRate) >= 0) {
                        t1 = 1;
                    } else {
                        t1 = 0;
                    }
                    if (Math.sin(2.0 * Math.PI * pulsos2 * frameCounter / sampleRate) >= 0) {
                        t2 = 1;
                    } else {
                        t2 = 0;
                    }
                    buffer[0][s] = t1 * Math.sin(2.0 * Math.PI * frecuencia1 * frameCounter / sampleRate);
                    buffer[1][s] = t2 * Math.sin(2.0 * Math.PI * frecuencia2 * frameCounter / sampleRate);
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
            }

            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        }
    }

    public void convertirVoz() throws Exception {
        //Inicializa un conversor para convertir (si es necesario) el audio de voz a la calidad apropiada (vozConvertida)
        Conversor conversor = new Conversor(this.voz, this.vozConvertida);
        if (conversor.necesitaConversion() == 1) {
            conversor.convertir();
            this.vozConvertida = conversor.getArchivoConvertido();
        } else if (conversor.necesitaConversion() == 0) {
            this.vozConvertida = this.voz;
        } else {
            error = 1;
            throw new Exception();
        }
    }

    public int convertir(String archivo, String destino) throws Exception {
        //Inicializa un conversor para convertir (si es necesario) el audio a la calidad apropiada
        Conversor conversor = new Conversor(archivo, destino);
        if (conversor.necesitaConversion() == 1) {
            conversor.convertir();
            errorMensaje = conversor.errorMensaje;
            return 1; // Necesita conversión y se realizó bien
        } else if (conversor.necesitaConversion() == 0) {
            errorMensaje = conversor.errorMensaje;
            return 2; // No necesita conversión
        } else {
            error = 1;
            errorMensaje = conversor.errorMensaje;
            throw new Exception();
        }
    }

    public void multiplicarOndas(String dirEntrada1, String dirEntrada2, String dirSalida) {
        // La duración del archivo1 manda!! Que el 1 sea el más corto...
        try {
            WavFile archivo1 = WavFile.openWavFile(new File(dirEntrada1));
            WavFile archivo2 = WavFile.openWavFile(new File(dirEntrada2));

            if (archivo1.getNumFrames() > archivo2.getNumFrames()) {
                throw new Exception("Error al integrar el isocrónico a la música...");
            }

            //Get the number of frames of the audio
            long numFrames = archivo1.getNumFrames();

            // Create a buffer of 100 frames for 2 channels
            double[][] bufferRead1 = new double[2][100];
            double[][] bufferRead2 = new double[2][100];

            // Create a silent WAV file con el formato deseado
            WavFile resultado = WavFile.newWavFile(new File(dirSalida), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a buffer of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead;

            do {
                // Read frames into buffer
                framesRead = archivo1.readFrames(bufferRead1, 100);
                archivo2.readFrames(bufferRead2, 100);

                // Loop through frames and create the silent buffer
                for (int s = 0; s < framesRead; s++) {
                    bufferWrite[0][s] = bufferRead1[0][s] * bufferRead2[0][s];
                    bufferWrite[1][s] = bufferRead1[1][s] * bufferRead2[1][s];
                }
                // Write the frames readed to the silent buffer into the silent file
                resultado.writeFrames(bufferWrite, framesRead);
            } while (framesRead != 0);

            // Cierra los streams abiertos para ahorrar recursos
            archivo1.close();
            archivo2.close();
            resultado.close();
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        }
    }

    public void hacerSilent(String dirEntrada, String dirSalida) {
        try {
            // Abre la voz convertida para modularla a altas frecuencias
            WavFile entrada = WavFile.openWavFile(new File(this.vozConvertida));

            //Get the number of frames of the audio
            long numFrames = entrada.getNumFrames();

            // Create a buffer of 100 frames for 2 channels
            double[][] bufferRead = new double[2][100];

            // Create a silent WAV file con el formato deseado
            WavFile silentSubliminal = WavFile.newWavFile(new File(dirSalida), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a buffer of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            // Initialise a local frame counter to make the sine wave
            long frameCounter = 0;

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead;

            do {
                // Read frames into buffer
                framesRead = entrada.readFrames(bufferRead, 100);

                // Loop through frames and create the silent buffer
                for (int s = 0; s < framesRead; s++) {
                    bufferWrite[0][s] = bufferRead[0][s] * (Math.sin(2.0 * Math.PI * frecuenciaModulacionSilent * frameCounter / frecuenciaMuestreo));
                    bufferWrite[1][s] = bufferRead[1][s] * (Math.sin(2.0 * Math.PI * frecuenciaModulacionSilent * frameCounter / frecuenciaMuestreo));
                    frameCounter++;
                }
                // Write the frames readed to the silent buffer into the silent file
                silentSubliminal.writeFrames(bufferWrite, framesRead);
            } while (framesRead != 0);

            // Cierra los streams abiertos para ahorrar recursos
            entrada.close();
            silentSubliminal.close();
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        }
    }

    // La onda dirOndas[0] es la onda principal y debe ser también la más corta para un correcto funcionamiento
    // Todas las ondas deben ser de la misma calidad
    public void sumarOndas(String[] dirOndas, int[] volumenes, String dirDestino) throws IOException {
        // Estar atento, si la suma es >1 o <-1 ponerle de valor 1 o -1 respectivamente
        java.util.List<WavFile> entrada = new ArrayList<>();
        WavFile audioCompletado = null;
        try {
            // Consultamos el número de archivos
            int nArchivos = dirOndas.length;
            if (nArchivos != volumenes.length) {
                System.err.println("El array de ondas y de volúmenes tiene distinta longitud.");
                throw new Exception();
            }

            // Abre todos los archivos en una lista

            for (String x : dirOndas) {
                entrada.add(WavFile.openWavFile(new File(x)));
            }

            java.util.List<Integer> volume = new ArrayList<>();
            for (int x : volumenes) {
                volume.add(x);
            }

            // Número de frames del audio principal (y más corto). Se supone el [0]
            long numFrames = entrada.get(0).getNumFrames();

            // Crea un buffer de 100 frames, para dos canales, para cada uno de los archivos
            double[][][] bufferRead = new double[nArchivos][2][100];

            // Create a silent WAV file con el formato deseado
            audioCompletado = WavFile.newWavFile(new File(dirDestino), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a buffer of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead = 0;

            do {
                for (int j = 0; j < 100; j++) {
                    bufferWrite[0][j] = 0;
                    bufferWrite[1][j] = 0;
                }
                for (int k = 0; k < nArchivos; k++) {
                    // Read frames into buffer
                    framesRead = entrada.get(k).readFrames(bufferRead[k], 100);
                    if (framesRead == 0) {
                        break;
                    }
                    // Loop through frames and create the buffer
                    for (int s = 0; s < framesRead; s++) {
                        bufferWrite[0][s] = bufferWrite[0][s] + (bufferRead[k][0][s] * volume.get(k) / 100);
                        bufferWrite[1][s] = bufferWrite[1][s] + (bufferRead[k][1][s] * volume.get(k) / 100);

                        if (bufferWrite[0][s] > 1 || bufferWrite[0][s] < -1 || bufferWrite[1][s] > 1 || bufferWrite[1][s] < -1) {
                            sobreModulacion++;
                        }

                        if (bufferWrite[0][s] > 1) {
                            bufferWrite[0][s] = 1;
                        }
                        if (bufferWrite[0][s] < -1) {
                            bufferWrite[0][s] = -1;
                        }
                        if (bufferWrite[1][s] > 1) {
                            bufferWrite[1][s] = 1;
                        }
                        if (bufferWrite[1][s] < -1) {
                            bufferWrite[1][s] = -1;
                        }
                    }
                }
                // Write the frames readed to the silent buffer into the silent file
                audioCompletado.writeFrames(bufferWrite, framesRead);
            } while (framesRead != 0);

            // Cierra los streams abiertos para ahorrar recursos
            for (int i = 0; i < nArchivos; i++) {
                entrada.get(i).close();
            }
            audioCompletado.close();
            porcentajeSobreModulacion = (double) (((double) sobreModulacion * (double) 100) / (double) numFrames);
            //DecimalFormat dec = new DecimalFormat("#0.00");
            //System.out.println(sobreModulacion + " frames sobremodulados de un total de " + numFrames + " que es " + dec.format((double)sobreModulacion*(double)100/(double)numFrames) + "%");
        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        } finally {
            try {
                for (int i = 0; i < entrada.size(); i++) {
                    entrada.get(i).close();
                }
                if (audioCompletado != null) {
                    audioCompletado.close();
                }
            } catch (Exception e) {
                System.out.println("No se han podido cerrar los streams tras sumas las pistas");
            }
        }
    }

    public static boolean borrarArchivo(String archivo) {
        errorStatic = false;
        try {
            if (archivo != null) {
                // A File object to represent the filename
                File f = new File(archivo);
                boolean intentar = true;

                // Make sure the file or directory exists and isn't write protected
                if (!f.exists()) {
                    intentar = false;
                }

                if (!f.canWrite()) {
                    intentar = false;
                }

                // If it is a directory, make sure it is empty
                if (f.isDirectory()) {
                    String[] files = f.list();
                    if (files.length > 0) {
                        intentar = false;
                    }
                }

                // Attempt to delete it
                boolean success = false;
                if (intentar) {
                    success = f.delete();
                }

                if (!success) {
                    System.err.println("Error al borrar el archivo temporal " + archivo + "\n");
                    errorStatic = true;
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            errorStatic = true;
            return false;
        }
    }

    public void borrarTemporales() {
        errorStatic = false;
        borrarArchivo(vozConvertida);
        borrarArchivo(silent);
        borrarArchivo(musica);
        borrarArchivo(isocronico);
        borrarArchivo(isocronicoIntegrado);
        borrarArchivo(isocronicoEnvelope);
        borrarArchivo(binaural);
        borrarArchivo(vocoder);
    }

    void copiarRecurso(String rutaEntradaRelativa, String rutaSalida) throws FileNotFoundException {
        InputStream leiendo = null;
        FileOutputStream scribiendo = null;
        try {
            leiendo = ModulateWav.class.getResourceAsStream(rutaEntradaRelativa);
            errorMensaje = "Archivo origen de la música = " + leiendo.toString();
            scribiendo = new FileOutputStream(rutaSalida);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = leiendo.read(buffer)) != -1) {
                scribiendo.write(buffer, 0, bytesRead);
            }
            leiendo.close();
            scribiendo.close();
        } catch (Exception e) {
            System.err.println("Error copiando el recurso fuera del paquete");
            System.out.println(e.getStackTrace());
        } finally {
            if (leiendo != null) {
                try {
                    leiendo.close();
                } catch (Exception e) {
                    errorMensaje = "El stream de entrada se mantiene abierto";
                }
            }
            if (scribiendo != null) {
                try {
                    scribiendo.close();
                } catch (Exception e) {
                    errorMensaje = "El stream de salida no se ha cerrado";
                }
            }
        }
    }

    //public File getFile(FileInputStream inStream, String rutaSalida) throws FileNotFoundException, IOException {
    public String getFile(String rutaEntradaRelativa, String rutaSalida) throws FileNotFoundException, IOException {

        // Entrada
        InputStream inStream = ModulateWav.class.getResourceAsStream(rutaEntradaRelativa);
        // Salida
        File file = new File(rutaSalida);

        OutputStream outStream = null;
        outStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];

        int length;
        //copy the file content in bytes 
        while ((length = inStream.read(buffer)) > 0) {

            outStream.write(buffer, 0, length);

        }


        outStream.close();

        return file.getAbsolutePath();

    }

    void copiar(String rutaEntrada, String rutaSalida) throws FileNotFoundException {
        try {
            InputStream leiendo = new FileInputStream(rutaEntrada);
            FileOutputStream scribiendo = new FileOutputStream(rutaSalida);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = leiendo.read(buffer)) != -1) {
                scribiendo.write(buffer, 0, bytesRead);
            }
            leiendo.close();
            scribiendo.close();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
