import os
import random

# Antes de trabajar en el codigo para la CNN, fue necesario hacer un ajuste, mas bien dicho, es necesario hacer un ajuste y una revision sobre el dataset
# La cantidad de imagenes que contenia el dataset era demasiado teniendo en cuenta el tiempo que tenemos para entrenarla y probarla, por lo que el siguiente codigo
# Tiene como objetivo el de eliminar una cantidad especifica de imagenes para entonces usarlas como entrenamiento y validacion
# Este codigo es un proceso automatizado especifico para el dataset que escogimos 

# Ruta principal que contiene las carpetas de letras (a, b, ..., z)
carpeta_principal = ''

# Extensión de los archivos a procesar, son .png
extension = ".png"

# Cantidad maxima de imágenes que queremos por subcarpeta (may o min) nosotros escojimos 450
max_imagenes = 450

# Recorriendo cada carpeta de letras (a, b, ..., z)
for letra in os.listdir(carpeta_principal):
    ruta_letra = os.path.join(carpeta_principal, letra)

    # Verificando que sea una carpeta lo que recorremos
    if os.path.isdir(ruta_letra):
        # Recorriendo las subcarpetas 'may' y 'min' dentro de cada letra en especifico
        for subcarpeta in ['may', 'min']:
            ruta_subcarpeta = os.path.join(ruta_letra, subcarpeta)

            # Verificando que la subcarpeta 'may' o 'min' existe
            if os.path.exists(ruta_subcarpeta):
                # Listamos todas las imágenes .png en la subcarpeta
                imagenes = [f for f in os.listdir(ruta_subcarpeta) if f.endswith(extension)]
                
                # Si hay más imágenes de las necesarias, eliminamos al azar
                if len(imagenes) > max_imagenes:
                    exceso = len(imagenes) - max_imagenes
                    # Elegiendo imagenes al azar
                    imagenes_a_eliminar = random.sample(imagenes, exceso)
                    
                    # Eliminamos las imágenes seleccionadas
                    for imagen in imagenes_a_eliminar:
                        ruta_imagen = os.path.join(ruta_subcarpeta, imagen)
                        os.remove(ruta_imagen)
                        print(f'Eliminada: {ruta_imagen}')

                else:
                    print(f'La carpeta {subcarpeta} en {letra} tiene {len(imagenes)} imágenes, no se necesita eliminar.')

print("Proceso completado.")
