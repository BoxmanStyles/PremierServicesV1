Comando para actualizar los archivos y que se suban al repositorio:

Codigo para hacer commints en todas las tablas:

# Rama main
git checkout main
git add .
git commit -m "feat: version final completa - Premier Services"
git push origin main

# Rama feature/auth-usuarios
git checkout feature/auth-usuarios
git add .
git commit -m "feat: modulo autenticacion y suscripciones completo"
git push origin feature/auth-usuarios

# Rama feature/proveedores
git checkout feature/proveedores
git add .
git commit -m "feat: modulo proveedores y portafolios completo"
git push origin feature/proveedores

# Rama feature/clientes
git checkout feature/clientes
git add .
git commit -m "feat: modulo clientes, catalogo y reservas completo"
git push origin feature/clientes

# Rama feature/admin-reportes
git checkout feature/admin-reportes
git add .
git commit -m "Arreglo en diseno admin panel"
git push origin feature/admin-reportes

