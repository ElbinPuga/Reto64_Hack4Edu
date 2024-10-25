import os
from tensorflow.keras.preprocessing import image # type: ignore
from tensorflow.keras.optimizers import Adam # type: ignore
from tensorflow.keras.applications.mobilenet_v2 import MobileNetV2, preprocess_input # type: ignore
from tensorflow.keras.preprocessing.image import ImageDataGenerator # type: ignore
from tensorflow.keras.utils import to_categorical # type: ignore
import numpy as np
import tensorflow as tf
import cv2

# Lista de las clases que usaremos para probar el modelo, pueden ser desde la misma carpeta de train
categories = os.listdir('')
categories.sort()

# Lista de las clases que usaremos para probar el modelo, pen este caso seria la direccion de las imagenes con las que queremos probar el modelo
test_path = ''
test = ImageDataGenerator(preprocessing_function=preprocess_input)

test_images = test.flow_from_directory(
    test_path,
    target_size=(224, 224),
    batch_size=30,
    class_mode='categorical'
)

# Cargar el modelo guardado, tenemos que recordar donde lo guardamos, su formato es h5
path_for_saved_model = ""
model = tf.keras.models.load_model(path_for_saved_model)

# Compilamos el modelo cargados
optimizer = Adam(learning_rate=0.0001)
model.compile(loss="categorical_crossentropy", optimizer=optimizer, metrics=['accuracy'])

# Directorio que contiene las imágenes de prueba sin clasificación aqui se debe poner el mismo directorio que se utilizo antes para test_path
test_dir = ''

# Nos aseguramos que el tamaño de las imágenes coincida con el tamaño que espera el modelo MobileNet
image_size = (224, 224)  # Declaramos el tamaño esperado

# Listamos todas las imágenes en la carpeta de test
test_images = [f for f in os.listdir(test_dir) if f.endswith(('.png', '.jpg', '.jpeg'))]

# Haciendo predicciones para cada imagen
for image_name in test_images:
    # Cargar y preprocesar la imagen
    img_path = os.path.join(test_dir, image_name)
    img = cv2.imread(img_path)
    img = cv2.resize(img, image_size)
    img = img.astype('float32') / 255.0  # Escalar los valores de los píxeles
    img = np.expand_dims(img, axis=0)  # Añadir dimensión para el batch
    
    # Haciendo la prediccion
    prediction = model.predict(img)
    
    # Obteniendo la clase predicha
    predicted_class = np.argmax(prediction, axis=-1)
    
    print(f"Imagen: {image_name}, Predicción: {predicted_class}")
