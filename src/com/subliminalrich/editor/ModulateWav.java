/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.subliminalrich.editor;

import java.io.*;
import java.util.ArrayList;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;

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
    int framesRead;
    boolean flagStop = false;
    JTextArea textArea = null;
    JScrollBar bar = null;

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

    public void repetir(int repeticiones, String origen, String destino) throws IOException {
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

                    // Create a bufferRead of 100 frames for 2 channels
                    double[][] bufferRead = new double[2][100];

                    // Create a bufferRead of 100 frames and 2 chanels where we are going to WRITE
                    double[][] bufferWrite = new double[2][100];

                    // Initialise a local frame counter to make the sine wave
                    long frameCounter = 0;

                    //Contador del número de frames leídos hasta el momento en el bucle
                    int framesRead;

                    do {
                        // Read frames into bufferRead
                        framesRead = entrada.readFrames(bufferRead, 100);

                        // Loop through frames and create the silent bufferRead
                        for (int s = 0; s < framesRead; s++) {
                            bufferWrite[0][s] = bufferRead[0][s];
                            bufferWrite[1][s] = bufferRead[1][s];
                            frameCounter++;
                        }
                        // Write the frames readed to the silent bufferRead into the silent file
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

                    // Create a bufferRead of 100 frames for 2 channels
                    double[][] bufferRead = new double[2][100];

                    // Create a bufferRead of 100 frames and 2 chanels where we are going to WRITE
                    double[][] bufferWrite = new double[2][100];

                    // Initialise a local frame counter to make the sine wave
                    long frameCounter = 0;

                    //Contador del número de frames leídos hasta el momento en el bucle
                    int framesRead;

                    do {
                        // Read frames into bufferRead
                        framesRead = entrada.readFrames(bufferRead, 100);

                        // Loop through frames and create the silent bufferRead
                        for (int s = 0; s < framesRead; s++) {
                            bufferWrite[0][s] = bufferRead[0][s];
                            bufferWrite[1][s] = bufferRead[1][s];
                            frameCounter++;
                        }
                        // Write the frames readed to the silent bufferRead into the silent file
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

            // Create a bufferRead of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the bufferRead size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the bufferRead, one tone per channel
                for (int s = 0; s < toWrite; s++, frameCounter++) {
                    buffer[0][s] = Math.sin(2.0 * Math.PI * frecuencia1 * frameCounter / sampleRate);
                    buffer[1][s] = Math.sin(2.0 * Math.PI * frecuencia2 * frameCounter / sampleRate);
                }

                // Write the bufferRead
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

            // Create a bufferRead of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the bufferRead size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the bufferRead, one tone per channel
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

                // Write the bufferRead
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

            // Create a bufferRead of 100 frames
            double[][] buffer = new double[2][100];

            // Initialise a local frame counter
            long frameCounter = 0;

            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the bufferRead size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > 100) ? 100 : (int) remaining;

                // Fill the bufferRead, one tone per channel
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

                // Write the bufferRead
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

            // Create a bufferRead of 100 frames for 2 channels
            double[][] bufferRead1 = new double[2][100];
            double[][] bufferRead2 = new double[2][100];

            // Create a silent WAV file con el formato deseado
            WavFile resultado = WavFile.newWavFile(new File(dirSalida), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a bufferRead of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead;

            do {
                // Read frames into bufferRead
                framesRead = archivo1.readFrames(bufferRead1, 100);
                archivo2.readFrames(bufferRead2, 100);

                // Loop through frames and create the silent bufferRead
                for (int s = 0; s < framesRead; s++) {
                    bufferWrite[0][s] = bufferRead1[0][s] * bufferRead2[0][s];
                    bufferWrite[1][s] = bufferRead1[1][s] * bufferRead2[1][s];
                }
                // Write the frames readed to the silent bufferRead into the silent file
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

            // Create a bufferRead of 100 frames for 2 channels
            double[][] bufferRead = new double[2][100];

            // Create a silent WAV file con el formato deseado
            WavFile silentSubliminal = WavFile.newWavFile(new File(dirSalida), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a bufferRead of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            // Initialise a local frame counter to make the sine wave
            long frameCounter = 0;

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead;

            do {
                // Read frames into bufferRead
                framesRead = entrada.readFrames(bufferRead, 100);

                // Loop through frames and create the silent bufferRead
                for (int s = 0; s < framesRead; s++) {
                    bufferWrite[0][s] = bufferRead[0][s] * (Math.sin(2.0 * Math.PI * frecuenciaModulacionSilent * frameCounter / frecuenciaMuestreo));
                    bufferWrite[1][s] = bufferRead[1][s] * (Math.sin(2.0 * Math.PI * frecuenciaModulacionSilent * frameCounter / frecuenciaMuestreo));
                    frameCounter++;
                }
                // Write the frames readed to the silent bufferRead into the silent file
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
    public void sumarOndas(String[] dirOndas, double[] volumenes, String dirDestino) throws IOException {
        // Estar atento, si la suma es >1 x <-1 ponerle de valor 1 x -1 respectivamente
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

            java.util.List<Double> volume = new ArrayList<>();
            for (double x : volumenes) {
                volume.add(x);
            }

            // Número de frames del audio principal (y más corto). Pillamos el último de la lista... Orden: Vocoder, silent, binaural, isocrónico, música
            long numFrames = entrada.get(entrada.size()-1).getNumFrames();

            // Crea un bufferRead de 100 frames, para dos canales, para cada uno de los archivos
            double[][][] bufferRead = new double[nArchivos][2][100];

            // Create a silent WAV file con el formato deseado
            audioCompletado = WavFile.newWavFile(new File(dirDestino), 2, numFrames, 24, frecuenciaMuestreo);

            // Create a bufferRead of 100 frames and 2 chanels where we are going to WRITE
            double[][] bufferWrite = new double[2][100];

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesRead = 0;

            do {
                for (int j = 0; j < 100; j++) {
                    bufferWrite[0][j] = 0;
                    bufferWrite[1][j] = 0;
                }
                for (int k = 0; k < nArchivos; k++) {
                    // Read frames into bufferRead
                    framesRead = entrada.get(k).readFrames(bufferRead[k], 100);
                    if (framesRead == 0) {
                        break;
                    }
                    // Loop through frames and create the bufferRead
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
                // Write the frames readed to the silent bufferRead into the silent file
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

    // La onda dirOndas[0] es la onda principal y debe ser también la más corta para un correcto funcionamiento
    // Todas las ondas deben ser de la misma calidad
    public void pegaCachoPorCacho(String rutaOnda, WavFile audioSumable) throws IOException {

        WavFile wavAnadir = null;
        try {

            // Abre la onda que vamos a añadir
            wavAnadir = WavFile.openWavFile(new File(rutaOnda));

            // Crea un bufferRead de 100 frames, para dos canales
            double[][] bufferRead = new double[2][100];

            //Contador del número de frames leídos hasta el momento en el bucle
            int framesLeidos = 0;

            do {

                // Read frames into bufferRead
                framesLeidos = wavAnadir.readFrames(bufferRead, 100);

                // Write the frames readed to the silent bufferRead into the silent file
                audioSumable.writeFrames(bufferRead, framesLeidos);

            } while (framesLeidos != 0);

        } catch (Exception e) {
            System.err.println(e);
            error = 1;
        } finally {
            try {
                wavAnadir.close();
            } catch (Exception e) {
                System.out.println("No se ha podido cerrar el stream");
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

    // Calcular el módulo de una frecuencia con componente imaginaria y real
    double absModulo(double real, double imaginario) {
        return Math.sqrt((real * real) + (imaginario * imaginario));
    }

    double[][] envelopeFreq(double[][] buffer, double objetivo1, double objetivo2, int sampleRate, double envelope1, double envelope2) {

        double[][] bufferOut = new double[buffer.length][buffer[0].length];

        int numMuestras = buffer[0].length;

        for (int i = 0; i < buffer[0].length; i++) {
            bufferOut[0][i] = envelope1 + (objetivo1 - envelope1) * i / numMuestras;
            bufferOut[1][i] = envelope2 + (objetivo1 - envelope1) * i / numMuestras;
        }

        return bufferOut;
    }

    double[][] normalizaDoubles(double[][] bufferIn) {

        // Esta sencilla función normaliza un array de doubles a valores entre 1 y -1
        double[][] bufferOut = new double[bufferIn.length][bufferIn[0].length];

        for (int j = 0; j < bufferIn.length; j++) {
            for (int i = 0; i < bufferIn[0].length; i++) {
                bufferOut[j][i] = (((2 * bufferIn[j][i]) / Double.MAX_VALUE) - 1);
            }
        }

        return bufferOut;
    }

    double[][] filtraFrecuencias(int menorQue, int mayorQue, double[][] bufferIn, int sampleRate) {

        // Filtra frecuencias que se encuentran entre el valor menorQue y mayorQue
        double[][] bufferOut = new double[2][bufferIn.length];

        for (int i = 0; i < bufferIn.length / 2; i++) {
            float frecuencia = (float) (i * sampleRate / bufferIn.length);
            if (frecuencia < menorQue && frecuencia > mayorQue) {
                bufferOut[0][i * 2] = 0;
                bufferOut[0][i * 2 + 1] = 0;
                bufferOut[1][i * 2] = 0;
                bufferOut[1][i * 2 + 1] = 0;
            } else {
                bufferOut[0][i * 2] = bufferIn[0][i * 2];
                bufferOut[0][i * 2 + 1] = bufferIn[0][i * 2 + 1];
                bufferOut[1][i * 2] = bufferIn[1][i * 2];
                bufferOut[1][i * 2 + 1] = bufferIn[1][i * 2 + 1];
            }
        }

        return bufferOut;
    }

    double[][] pasaFrecuencias(float mayorIgualQue, float menorQue, double[][] bufferIn, int sampleRate) {

        // deja pasar solamente las frecuencias entre menorQue y mayorQue
        double[][] bufferOut = new double[bufferIn.length][bufferIn[0].length];
        int frames = bufferIn[0].length;

        // Miramos por parejas porque son parte real e imaginaria
        for (int x = 0; x < bufferOut.length; x++) {
            for (int i = 0; i < frames / 4; i++) { // Partido por 2 porque miramos por parejas, y por otros 2 porque las frecuencias son espejadas
                float frecuencia = (float) (i * sampleRate / bufferIn[0].length);
                if (frecuencia >= mayorIgualQue && frecuencia < menorQue) {
                    bufferOut[x][i * 2] = bufferIn[x][i * 2];
                    bufferOut[x][i * 2 + 1] = bufferIn[x][i * 2 + 1];
                    bufferOut[x][frames - 1 - i * 2 - 1] = bufferIn[x][frames - 1 - i * 2 - 1];
                    bufferOut[x][frames - 1 - i * 2] = bufferIn[x][frames - 1 - i * 2];
                } else {
                    bufferOut[x][i * 2] = 0;
                    bufferOut[x][i * 2 + 1] = 0;
                    bufferOut[x][frames - 1 - i * 2 - 1] = 0;
                    bufferOut[x][frames - 1 - i * 2] = 0;
                }
            }
        }

        return bufferOut;
    }

    double[][] WavTofft(long sampleRate, int samplesNum, WavFile wavFile) throws Exception {
        // Solamente acepta sonidos estéreo de 2 canales

        if (wavFile.getNumChannels() != 2) {
            throw new Exception("Debe ser un audio de 2 canales");
        }

        double[][] fftData = new double[2][samplesNum * 2]; // Parte real e imaginaria
        double[][] buffer = new double[2][samplesNum]; // Buffer de lectura del archivo de sonido
        this.framesRead = wavFile.readFrames(buffer, samplesNum);
        // Le pasamos una ventana al fragmento leído para evitar frequency leaqage
        buffer = windowear(buffer);

        // Copiamos los canales de sonido al array del FFT
        for (int i = 0; i < framesRead; i++) {
            fftData[0][i * 2] = buffer[0][i];
            fftData[0][i * 2 + 1] = 0;
            fftData[1][i * 2] = buffer[1][i];
            fftData[1][i * 2 + 1] = 0;
        }

        if (this.framesRead == 0) {
            this.flagStop = true;
        }


        DoubleFFT_1D fft = new DoubleFFT_1D(samplesNum);
        // Cambiamos el array a la transformada de fourier
        fft.complexForward(fftData[0]);
        fft.complexForward(fftData[1]);



        // Parametros para observar el módulo del vector real/imaginario representando la frecuencia
        // Solamente se mira la mitad del espectro, la otra mitad es lo mismo espejado

        /*for(int i = 0; i < samplesNum/2; i++){
         double value1 = Math.sqrt(fftData[0][2*i]*fftData[0][2*i]+fftData[0][2*i+1]*fftData[0][2*i+1]);
         double value2 = Math.sqrt(fftData[1][2*i]*fftData[1][2*i]+fftData[1][2*i+1]*fftData[1][2*i+1]);
         if(value1 > 0.1 || value2 > 0.1){
         System.out.println((((i)*sampleRate)/samplesNum) + " Herzios " + value1 + " " + value2);
         }
         }*/

        return fftData;
    }

    double fftToWav(WavFile wavFile, double[][] buffer, int samplesNum) throws IOException {
        // Solamente acepta sonidos estéreo de 2 canales

        double maximo = Double.MIN_VALUE;

        DoubleFFT_1D fft = new DoubleFFT_1D(samplesNum);
        fft.complexInverse(buffer[0], true);
        fft.complexInverse(buffer[1], true);

        double[][] bufferWrite = new double[2][samplesNum];
        // Escribimos los datos al bufferRead de escritura de las posiciones pares
        for (int i = 0; i < samplesNum; i++) {
            bufferWrite[0][i] = buffer[0][i * 2];
            if (Math.abs(bufferWrite[0][i]) > maximo) {
                maximo = Math.abs(bufferWrite[0][i]);
            }
            bufferWrite[1][i] = buffer[1][i * 2];
            if (Math.abs(bufferWrite[1][i]) > maximo) {
                maximo = Math.abs(bufferWrite[0][i]);
            }
        }

        try {
            wavFile.writeFrames(bufferWrite, this.framesRead);
        } catch (Exception e) {
            this.error = 1;
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
        return Math.abs(maximo);
    }

    double[][] fftToArray(double[][] buffer, int samplesNum) {

        DoubleFFT_1D fft = new DoubleFFT_1D(samplesNum);
        fft.complexInverse(buffer[0], true);
        fft.complexInverse(buffer[1], true);

        return buffer;
    }
    // Variables de control en las subfunciones
    int freqBajaAbs = 100;
    int anchoBandaAbs = 100;
    int saltoAbs = 100;

    public void vocodificar(String rutaVoz, String rutaMusica, String rutaOut, int bandas, int frameMilisegundos, int sampleRate) throws IOException, WavFileException {

        // Declaramos variables globales
        WavFile wavVoz = null;
        WavFile wavMusica = null;

        // Variables de flujo y control
        int samplesNum = (int) (frameMilisegundos * 0.001 * sampleRate);
        this.flagStop = false;

        // Inicializamos las rutas de los envelopers
        String[] envRut = new String[bandas];
        for (int i = 0; i < bandas; i++) {
            envRut[i] = getRutaTemporal("envR" + i);
        }

        // Inicializamos las rutas de la música filtrada
        String[] musRut = new String[bandas];
        for (int i = 0; i < bandas; i++) {
            musRut[i] = getRutaTemporal("musR" + i);
        }

        try {
            // Inicializamos los archivos principales
            wavVoz = WavFile.openWavFile(new File(rutaVoz));
            wavMusica = WavFile.openWavFile(new File(rutaMusica));

            double[][] envelope = new double[bandas][2]; //bandas, canal
            for (int i = 0; i < envelope.length; i++) {
                for (int j = 0; j < envelope[i].length; j++) {
                    envelope[i][j] = 0;
                }
            }

            if (this.textArea != null && this.bar != null) {
                textArea.append("=> Analizando la voz\n");
                bar.setValue(bar.getMaximum());
            }
            // Obtenemos el módulo máximo de las componentes filtradas de la voz, y ahora vamos a normalizarlo y a pasarlo a archivos Wav
            double maximo = valorMaximoVoz(sampleRate, samplesNum, wavVoz, bandas, envelope);

            if (this.textArea != null && this.bar != null) {
                textArea.append("=> Generando envelope\n");
                bar.setValue(bar.getMaximum());
            }
            // Reiniciamos la pista de voz para procesar el envelope
            wavVoz.close();
            wavVoz = WavFile.openWavFile(new File(rutaVoz));
            // Reiniciamos el flag del envelope
            for (int i = 0; i < envelope.length; i++) {
                for (int j = 0; j < envelope[i].length; j++) {
                    envelope[i][j] = 0;
                }
            }
            // Reiniciamos el flag de parada
            this.flagStop = false;

            // Dividimos los envelopes de la voz en las bandas de frecuencia seleccionadas
            divideBandasVoz(sampleRate, samplesNum, wavVoz, bandas, envelope, maximo, envRut);

            if (this.textArea != null && this.bar != null) {
                textArea.append("=> Procesando la música\n");
                bar.setValue(bar.getMaximum());
            }
            // Reiniciamos el flag de parada
            this.flagStop = false;

            // Dividimos la pista de música en las bandas de frecuencia seleccionadas
            maximo = divideBandasMusica(wavMusica, musRut, sampleRate, bandas, samplesNum);

            // Y les subimos el volumen
            String[] musVol = new String[bandas];
            for (int i = 0; i < bandas; i++) {
                musVol[i] = getRutaTemporal("musVo" + i);
            }
            subirVolumen(musRut, musVol, maximo, 0.9);

            if (this.textArea != null && this.bar != null) {
                textArea.append("=> Integrando el perfil de la voz en la música\n");
                bar.setValue(bar.getMaximum());
            }
            // Multiplicamos cada banda de la música por su envelope
            String[] banDef = new String[bandas];
            for (int i = 0; i < bandas; i++) {
                banDef[i] = getRutaTemporal("banDe" + i);
            }
            followerArray(musVol, envRut, banDef);
            
            if (this.textArea != null && this.bar != null) {
                textArea.append("=> Juntando todas las bandas\n");
                bar.setValue(bar.getMaximum());
            }

            // Juntamos todas las bandas en un solo archivo.
            double[] vorumenes = new double[bandas];
            for (int i = 0; i < bandas; i++) {
                vorumenes[i] = 100;
            }
            sumarOndas(banDef, vorumenes, rutaOut);

            // Borramos las bandas temporales
            for (int i = 0; i < bandas; i++) {
                borrarArchivo(banDef[i]);
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            wavVoz.close();
            wavMusica.close();
        }
    }

    void subirVolumen(String[] entradas, String[] salidas, double maximo, double volumen) throws IOException, WavFileException {
        WavFile[] in = new WavFile[entradas.length];
        for (int i = 0; i < entradas.length; i++) {
            in[i] = WavFile.openWavFile(new File(entradas[i]));
        }
        WavFile[] out = new WavFile[entradas.length];
        for (int i = 0; i < entradas.length; i++) {
            out[i] = WavFile.newWavFile(new File(salidas[i]), 2, in[i].getNumFrames(), 24, in[i].getSampleRate());
        }

        double[][] buffer = new double[2][100];
        for (int i = 0; i < entradas.length; i++) {
            int leidos = 0;
            while ((leidos = in[i].readFrames(buffer, 100)) != 0) {
                for (int k = 0; k < buffer.length; k++) {
                    for (int j = 0; j < buffer[0].length; j++) {
                        buffer[k][j] = (buffer[k][j] / maximo) * volumen;
                    }
                }
                out[i].writeFrames(buffer, leidos);
            }
            in[i].close();
            out[i].close();
            borrarArchivo(entradas[i]);
        }
    }

    void followerArray(String[] musica, String[] voz, String[] rutaSeguidores) throws IOException, WavFileException {
        for (int i = 0; i < rutaSeguidores.length; i++) {
            multiplicarOndas(voz[i], musica[i], rutaSeguidores[i]);
            borrarArchivo(musica[i]);
            borrarArchivo(voz[i]);
        }
    }
    int ventana = 1;

    double[][] windowear(double[][] buffer) {
        double[][] buffOut = new double[buffer.length][buffer[0].length];

        int sin = 0;
        int triangular = 1;
        int hanningZero = 2;
        int hanning = 3;
        int blackmanHarris = 4;
        int flatTop = 5;
        int hann = 6;
        int hamming = 7;
        int barlett = 8;
        int barletthan = 9;

        int N = buffer[0].length;
        float a0 = (float) 0;
        float a1 = (float) 0;
        float a2 = (float) 0;
        float a3 = (float) 0;
        float a4 = (float) 0;
        int L = 0;

        if (ventana == barletthan) {
            a0 = (float) 0.62;
            a1 = (float) 0.48;
            a2 = (float) 0.38;
        }
        if (ventana == hamming) {
            a0 = (float) 0.53836;
            a1 = (float) 0.46164;
        }
        if (ventana == hann) {
            a0 = (float) 0.5;
            a1 = (float) 0.5;
        }

        if (ventana == blackmanHarris) {
            // Blackman-Harris
            a0 = (float) 0.35875;
            a1 = (float) 0.48829;
            a2 = (float) 0.14128;
            a3 = (float) 0.01168;
        }
        if (ventana == flatTop) {
            // Flat-Top
            a0 = (float) 1;
            a1 = (float) 1.93;
            a2 = (float) 1.29;
            a3 = (float) 0.338;
            a4 = (float) 0.032;
        }

        if (ventana == triangular) {
            //Triangular
            L = N;
            //L = N-1;
            //L = N+1;
        }

        for (int i = 0; i < buffer.length; i++) {
            for (int j = 0; j < buffer[0].length; j++) {
                int n = j + 1; //Porque "j" empieza en "0"

                if (ventana == barletthan) {
                    buffOut[i][j] = buffer[i][j] * (a0 - a1 * Math.abs((n / (N - 1)) - 1 / 2) - a2 * Math.cos((2 * Math.PI * n) / (N - 1)));
                }
                if (ventana == barlett) {
                    buffOut[i][j] = buffer[i][j] * (((N - 1) / 2) - Math.abs((n) - ((N - 1) / 2)));
                }
                if (ventana == sin) {
                    //Esto es Sin ventana
                    buffOut[i][j] = buffer[i][j];
                }
                if (ventana == hamming) {
                    buffOut[i][j] = buffer[i][j] * (a0 - a1 * Math.cos((2 * Math.PI * n) / (N - 1)));
                }
                if (ventana == hann) {
                    buffOut[i][j] = buffer[i][j] * (a0 - a1 * Math.cos((2 * Math.PI * n) / (N - 1)));
                }
                if (ventana == triangular) {
                    //Triangular
                    buffOut[i][j] = buffer[i][j] * (1 - Math.abs((n - ((N - 1) / 2)) / (N + L) / 2));
                }
                if (ventana == blackmanHarris) {
                    // Esta es la Blackman–Harris window para transformadas de fourier discretas
                    buffOut[i][j] = buffer[i][j] * (a0 - a1 * Math.cos((2 * Math.PI * n) / (N - 1)) + a2 * Math.cos((4 * Math.PI * n) / (N - 1)) - a3 * Math.cos((6 * Math.PI * n) / (N - 1)));
                }
                if (ventana == flatTop) {
                    // Esta es Flat-Top
                    buffOut[i][j] = buffer[i][j] * (a0 - a1 * Math.cos((2 * Math.PI * n) / (N - 1)) + a2 * Math.cos((4 * Math.PI * n) / (N - 1)) - a3 * Math.cos((6 * Math.PI * n) / (N - 1)) + a4 * Math.cos((8 * Math.PI * n) / (N - 1)));
                }
                if (ventana == hanning) {
                    // Hanning
                    buffOut[i][j] = buffer[i][j] * (0.5 * (1 - Math.cos((2 * Math.PI * n) / (N - 1))));
                }
                if (ventana == hanningZero) {
                    // Hanning
                    buffOut[i][j] = buffer[i][j] * (0.5 * (1 + Math.cos((2 * Math.PI * n) / (N - 1))));
                }
            }
        }

        return buffOut;
    }

    double divideBandasMusica(WavFile musica, String[] rutas, int sampleRate, int bandas, int samplesNum) throws Exception {
        WavFile[] musWav = new WavFile[bandas];
        int aviso = 5;
        for (int i = 0; i < bandas; i++) {
            musWav[i] = WavFile.newWavFile(new File(rutas[i]), 2, musica.getNumFrames(), 24, sampleRate);
        }
        double maximo = Double.MIN_VALUE;
        do {
            // Pasamos la música a fourier
            double[][] fftMusica = WavTofft(sampleRate, samplesNum, musica);

            //Filtramos las frecuencias por paso banda
            int freqBaja = freqBajaAbs;
            int anchoBanda = anchoBandaAbs;
            int salto = saltoAbs;
            double[][][] filtMusica = new double[bandas][fftMusica.length][samplesNum]; //Bandas, canales, frames
            for (int i = 0; i < bandas; i++) {
                filtMusica[i] = pasaFrecuencias(freqBaja, freqBaja + anchoBanda, fftMusica, sampleRate);
                freqBaja += salto;
            }

            // Pasamos las series de fourier a archivos separados
            for (int i = 0; i < bandas; i++) {
                double maxTem = fftToWav(musWav[i], filtMusica[i], samplesNum);
                if (maxTem > maximo) {
                    maximo = maxTem;
                }
            }
            
            // Pasamos porcentaje por el TextArea para informar al espectador :D
            double porcentaje = (double)(musica.getNumFrames()-musica.getFramesRemaining()) / musica.getNumFrames();
            if (porcentaje >= (double) aviso/100) {
                if (this.textArea != null && this.bar != null) {
                    textArea.append(aviso + "%\n");
                    bar.setValue(bar.getMaximum());
                }
                while(porcentaje >= (double)aviso/100){
                aviso = aviso+5;
                }
            }

        } while (this.flagStop == false);
        for (int i = 0; i < bandas; i++) {
            musWav[i].close();
        }
        return maximo;
    }

    double valorMaximoVoz(int sampleRate, int samplesNum, WavFile wavVoz, int bandas, double[][] envelope) throws Exception {
        double maximo = Double.MIN_VALUE;
        int aviso = 10;
        do {
            // Pasamos la voz a Fourier
            double[][] fftVoz = WavTofft(sampleRate, samplesNum, wavVoz);

            // Filtramos las frecuencias por paso banda
            int freqBaja = freqBajaAbs;
            int anchoBanda = anchoBandaAbs;
            int salto = saltoAbs;
            double[][][] filtVoz = new double[bandas][fftVoz.length][samplesNum]; // Bandas, canales, frames
            for (int i = 0; i < bandas; i++) {
                filtVoz[i] = pasaFrecuencias(freqBaja, freqBaja + anchoBanda, fftVoz, sampleRate);
                freqBaja += salto;
            }

            // Calculamos los objetivos para el envelope
            double[][] objetivos = new double[bandas][2]; // bandas, canal
            for (int i = 0; i < objetivos.length; i++) {
                for (int j = 0; j < objetivos[i].length; j++) {
                    objetivos[i][j] = calculaObjetivo(filtVoz[i][j]);
                    if (objetivos[i][j] > maximo) {
                        maximo = objetivos[i][j];
                    }
                }
            }

            // Hacemos el envelope de la voz
            /*double[][][] envVoz = new double[bandas][2][samplesNum];
             for (int i = 0; i < bandas; i++) {
             envVoz[i] = envelopeFreq(filtVoz[i], objetivos[i][0], objetivos[i][1], sampleRate, envelope[i][0], envelope[i][1]);
             envelope[i][0] = objetivos[i][0];
             envelope[i][1] = objetivos[i][1];
             }*/
            
            // Pasamos porcentaje por el TextArea para informar al espectador :D
            double porcentaje = (double)(wavVoz.getNumFrames()-wavVoz.getFramesRemaining()) / wavVoz.getNumFrames();
            if (porcentaje >= (double) aviso/100) {
                if (this.textArea != null && this.bar != null) {
                    textArea.append(aviso + "%\n");
                    bar.setValue(bar.getMaximum());
                }
                while(porcentaje >= (double)aviso/100){
                aviso = aviso+10;
                }
            }

        } while (this.flagStop == false);
        return maximo;
    }

    void divideBandasVoz(int sampleRate, int samplesNum, WavFile wavVoz, int bandas, double[][] envelope, double maximo, String[] envRut) throws Exception {
        WavFile[] envWav = new WavFile[bandas];
        int aviso = 10;
        for (int i = 0; i < bandas; i++) {
            envWav[i] = WavFile.newWavFile(new File(envRut[i]), 2, wavVoz.getNumFrames(), 24, sampleRate);
        }
        do {
            // Pasamos la voz a Fourier
            double[][] fftVoz = WavTofft(sampleRate, samplesNum, wavVoz);

            // Filtramos las frecuencias por paso banda
            int freqBaja = freqBajaAbs;
            int anchoBanda = anchoBandaAbs;
            int salto = saltoAbs;
            double[][][] filtVoz = new double[bandas][fftVoz.length][samplesNum]; // Bandas, canales, frames
            for (int i = 0; i < bandas; i++) {
                filtVoz[i] = pasaFrecuencias(freqBaja, freqBaja + anchoBanda, fftVoz, sampleRate);
                freqBaja += salto;
            }

            // Calculamos los objetivos para el envelope
            double[][] objetivos = new double[bandas][2]; // bandas, canal
            for (int i = 0; i < objetivos.length; i++) {
                for (int j = 0; j < objetivos[i].length; j++) {
                    objetivos[i][j] = calculaObjetivo(filtVoz[i][j]) / maximo;
                }
            }

            // Hacemos el envelope de la voz
            double[][][] envVoz = new double[bandas][2][samplesNum];
            for (int i = 0; i < bandas; i++) {
                envVoz[i] = envelopeFreq(filtVoz[i], objetivos[i][0], objetivos[i][1], sampleRate, envelope[i][0], envelope[i][1]);
                envelope[i][0] = objetivos[i][0];
                envelope[i][1] = objetivos[i][1];
            }

            // Escribimos el envelope a un archivo
            for (int i = 0; i < bandas; i++) {
                envWav[i].writeFrames(envVoz[i], samplesNum);
            }
            
            // Pasamos porcentaje por el TextArea para informar al espectador :D
            double porcentaje = (double)(wavVoz.getNumFrames()-wavVoz.getFramesRemaining()) / wavVoz.getNumFrames();
            if (porcentaje >= (double) aviso/100) {
                if (this.textArea != null && this.bar != null) {
                    textArea.append(aviso + "%\n");
                    bar.setValue(bar.getMaximum());
                }
                while(porcentaje >= (double)aviso/100){
                aviso = aviso+10;
                }
            }

        } while (this.flagStop == false);
        for (int i = 0; i < bandas; i++) {
            envWav[i].close();
        }
    }

    String getRutaTemporal(String nombre) {
        String rutaVozTemporal = new File(voz).getParent() + System.getProperty("file.separator") + nombre + Long.toString(System.nanoTime()) + ".wav";
        return rutaVozTemporal;
    }

    String getDireccionTemporal(String nombre) {
        String rutaVozTemporal = new File(voz).getParent() + System.getProperty("file.separator") + nombre + Long.toString(System.nanoTime()) + ".txt";
        return rutaVozTemporal;
    }

    double calculaObjetivo(double[] fourier) {
        double objetivo = 0;

        for (int i = 0; i < fourier.length / 2; i++) {
            double modulo = Math.sqrt(fourier[i * 2] * fourier[i * 2] + fourier[i * 2 + 1] * fourier[i * 2 + 1]);
            objetivo += modulo;
        }

        return objetivo;
    }

    void escribir(BufferedWriter escritor, double[][] array) throws IOException {
        for (int i = 0; i < array.length; i++) { // canales
            for (int j = 0; j < array[0].length; j++) { // frames dentro del canal
                escritor.write(Double.toString(array[i][j]) + ";");
            }
            escritor.newLine();// entre cada buffer de canal hay una línea, y se alternan i/d
        }
    }

    double detectaMaximoEnArchivo(FileReader archivo) throws IOException {
        double maximo = Double.MIN_VALUE;
        String linea = "";

        try (BufferedReader lector = new BufferedReader(archivo)) {
            while ((linea = lector.readLine()) != null) {
                String[] iteraciones = linea.split(";");
                for (int i = 0; i < iteraciones.length; i++) {
                    if (Double.parseDouble(iteraciones[i]) > maximo) {
                        maximo = Double.parseDouble(iteraciones[i]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return maximo;
    }

    void normalizaArchivo(FileReader lectura, FileWriter escritura, double normalizante) throws IOException {
        try (BufferedReader lector = new BufferedReader(lectura); BufferedWriter escritor = new BufferedWriter(escritura)) {

            String linea = "";

            while ((linea = lector.readLine()) != null) {
                String[] iteraciones = linea.split(";");
                for (int i = 0; i < iteraciones.length; i++) {
                    escritor.write(Double.toString(Double.parseDouble(iteraciones[i]) / normalizante) + ";");
                    escritor.newLine();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    void multiplicaArchivos(FileReader lectura1, FileReader lectura2, FileWriter escritura) {
        try (BufferedReader lector1 = new BufferedReader(lectura1); BufferedReader lector2 = new BufferedReader(lectura2); BufferedWriter escritor = new BufferedWriter(escritura)) {
            String linea1 = "";
            String linea2 = "";

            while ((linea1 = lector1.readLine()) != null && (linea2 = lector2.readLine()) != null) {
                String[] iteraciones1 = linea1.split(";");
                String[] iteraciones2 = linea2.split(";");
                if (iteraciones1.length != iteraciones2.length) {
                    throw new Exception();
                }
                for (int i = 0; i < iteraciones1.length; i++) {
                    escritor.write(Double.toString(Double.parseDouble(iteraciones1[i]) * Double.parseDouble(iteraciones2[i])) + ";");
                    escritor.newLine();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }

    void sumaArchivos(FileReader[] lectura1, FileWriter escritura) {
        try {
            BufferedReader[] lector1 = new BufferedReader[lectura1.length];
            BufferedWriter escritor = new BufferedWriter(escritura);

            for (int i = 0; i < lector1.length; i++) {
                lector1[i] = new BufferedReader(lectura1[i]);
            }

            String[] linea1 = new String[lector1.length];
            double[] suma = null;
            boolean flagParada = false;

            while (flagParada == false) {
                for (int i = 0; i < lector1.length; i++) { // Las 3 bandas
                    linea1[i] = lector1[i].readLine();
                    if (linea1[i] == null) {
                        flagParada = true;
                    }
                    String[] iterator1 = linea1[i].split(";");
                    if (i == 0) {
                        suma = new double[iterator1.length];
                    }
                    for (int j = 0; j < iterator1.length; j++) {
                        suma[j] += Double.parseDouble(iterator1[j]);
                    }
                }
                for (int i = 0; i < suma.length; i++) {
                    escritor.write(Double.toString(suma[i]) + ";");
                }
                escritor.newLine();
            }
            escritor.close();
            for (int i = 0; i < lector1.length; i++) {
                lector1[i].close();
            }
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }

    void archivoToWav(FileReader canal1, FileReader canal2, WavFile salida) {
        try (BufferedReader lector1 = new BufferedReader(canal1); BufferedReader lector2 = new BufferedReader(canal2);) {
            String linea1 = "";
            String linea2 = "";

            while ((linea1 = lector1.readLine()) != null && (linea2 = lector2.readLine()) != null) {
                String[] iterador1 = linea1.split(";");
                String[] iterador2 = linea2.split(";");

                double[][] buffer = new double[2][iterador1.length];

                for (int i = 0; i < iterador1.length; i++) {
                    buffer[0][i] = Double.parseDouble(iterador1[i]);
                    buffer[1][i] = Double.parseDouble(iterador2[i]);
                }

                salida.writeFrames(buffer, buffer[0].length);
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    double[][] juntaEnvelopes(double[][] fftVoz, int sampleRate, int bandas, float anchoBanda, float freqBaja, double[][] objetivo, double normalizador, double[][] envelope) {
        double[][][] ondaFiltrada = new double[bandas][fftVoz.length][fftVoz[0].length];
        // Vamos a realizar el proceso X veces, una para cada banda
        for (int k = 0; k < bandas; k++) {

            // Creamos un array filtrado de cada pista
            ondaFiltrada[k] = pasaFrecuencias(freqBaja, freqBaja + anchoBanda, fftVoz, sampleRate);

            objetivo[k][0] = 0;
            objetivo[k][1] = 0;
            for (int y = 0; y < ondaFiltrada[0].length / 2; y++) {
                double modulo = Math.sqrt((ondaFiltrada[k][0][y * 2] * ondaFiltrada[k][0][y * 2]) + (ondaFiltrada[k][0][y * 2 + 1] * ondaFiltrada[k][0][y * 2 + 1]));
                objetivo[k][0] += modulo;
                modulo = Math.sqrt((ondaFiltrada[k][1][y * 2] * ondaFiltrada[k][1][y * 2]) + (ondaFiltrada[k][1][y * 2 + 1] * ondaFiltrada[k][1][y * 2 + 1]));
                objetivo[k][1] += modulo;
            }

            envelopeFreq(ondaFiltrada[k], objetivo[k][0], objetivo[k][1], sampleRate, envelope[0][k], envelope[1][k]);
            envelope[0][k] = objetivo[k][0];
            envelope[1][k] = objetivo[k][1];
        }

        double[][] vozFilTot = new double[fftVoz.length][fftVoz[0].length];
        for (int i = 0; i < vozFilTot.length; i++) {
            for (int j = 0; j < vozFilTot[0].length; j++) {
                vozFilTot[i][j] = 0;
            }
        }
        for (int i = 0; i < bandas; i++) {
            for (int j = 0; j < vozFilTot.length; j++) {
                for (int k = 0; k < vozFilTot[0].length; k++) {
                    vozFilTot[j][k] += ondaFiltrada[i][j][k];
                }
            }
        }
        return vozFilTot;
    }

    void escribirBuffer(BufferedWriter archivo, double[] valores) {
        try {
            for (double valor : valores) {
                archivo.write(Double.toString(valor) + ";");
            }
            archivo.newLine();
        } catch (Exception e) {
        } finally {
        }
    }

    double[] leerBuffer(BufferedReader buffer) throws IOException {
        String linea = "";
        double[] retornar;
        linea = buffer.readLine();
        String[] devuelve = linea.split(";");
        retornar = new double[devuelve.length];
        for (int i = 0; i < devuelve.length; i++) {
            retornar[i] = Double.parseDouble(devuelve[i]);
        }

        return retornar;
    }
}
