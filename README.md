# Lemac DataLab – MVP de autorregistro (DSY1105)

App Android (Kotlin + Jetpack Compose) de autorregistro en estado de ánimo, neurodesarrollo,
habilidades DBT y adicciones. **Solo datos ficticios.** No diagnostica, no indica tratamientos
ni es un servicio de urgencia.

## Arquitectura (MVVM)

Pantalla → ViewModel → Repository → DAO → Room (SQLite)

- `model/` Registro, Ambito, Catalogo (opciones sintéticas)
- `data/` RegistroEntity, RegistroDao, AppDatabase (Room), RegistroRepository, PreferenciasRepository (DataStore)
- `viewmodel/` RegistroViewModel + RegistroFormState (validaciones)
- `ui/` pantallas Compose (Home, Register, Settings) y componentes reutilizables
- `navigation/` NavHost con transiciones animadas
- `notifications/` recordatorios locales (recurso nativo)

## Avance
- Semana 9: navegación, formularios y validaciones, ViewModel, estado, animaciones, notificaciones.
- Semana 10: Room (Entity, DAO con CRUD, Database), Repository, corrutinas y persistencia real.
