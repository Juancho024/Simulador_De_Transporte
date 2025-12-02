**# 🚀 Simulador de Ruta de Transportes**

**_Realizado por Juan Esteban Rijo Pineda y Jean Carlos Cruz, Estudiantes de ICC._**

## 💡 Descripción General
* Este proyecto es un simulador de rutas de transporte diseñado para ayudarte a encontrar la ruta más eficiente entre dos puntos dentro de una red de transporte.
* El sistema permite optimizar rutas según tiempo, costo, distancia, cantidad de paradas y otros parámetros relevantes.
* Está implementado usando grafos dirigidos, aprovechando varios algoritmos clásicos para obtener rutas óptimas como:
   1. Dijkstra
   2. Floyd-Warshall
   3. Bellman-Ford

El objetivo es ofrecer una herramienta flexible, visual y eficiente para analizar rutas reales o simuladas en un entorno amigable.

## ⚙️ Características:

### Manejo para realizar el registro de Rutas y Paradas:
* El programa cuenta con paneles dedicados para registrar rutas y paradas.
* Te solicita todos los parámetros necesarios como distancia, costo, tiempo y dirección, garantizando un registro completo.
* También permite editar o eliminar rutas/paradas ya creadas.

## **Listado de Rutas y Paradas:**
* El programa puede mostrar listados organizados donde se visualiza toda la información registrada.
* Incluye detalles como conexiones, pesos, tiempos y demás datos relevantes.

## **Visualización de Grafos:**
* El sistema incluye un menú principal donde se pueden visualizar los grafos generados.
* Se muestran todas las rutas, direcciones, pesos y conexiones entre paradas.
* Permite comprender de forma clara cómo se estructura la red de transporte creada por el usuario.

## **Algoritmos de Búsqueda de Rutas:**

* Implementación de los principales algoritmos de caminos más cortos:
* Dijkstra para grafos con pesos positivos.
* Floyd-Warshall para caminos entre todos los nodos.
* Bellman-Ford para soportar pesos negativos y detectar ciclos.
* El usuario puede elegir el algoritmo preferido y analizar distintas rutas encontradas según diversos criterios.

## **📘 Objetivo del Proyecto:**

* Aplicar estructuras de datos avanzadas como grafos dirigidos.
* Implementar algoritmos clásicos de optimización de rutas.
* Ofrecer una plataforma intuitiva para registrar, administrar y visualizar rutas.
* Simular redes de transporte funcionales y realistas.
