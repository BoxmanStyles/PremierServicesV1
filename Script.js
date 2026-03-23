// Application State
let currentUser = null;
let selectedUserType = null;
let selectedPlan = null;
let currentSupplier = null;

// Sample Data
const suppliers = [
  {
    id: 1,
    name: "Delicias Gourmet",
    category: "catering",
    categoryLabel: "Catering",
    location: "santo-domingo",
    locationLabel: "Santo Domingo",
    rating: 4.9,
    reviews: 245,
    description: "Especialistas en alta cocina para eventos exclusivos. Menus personalizados con ingredientes frescos y presentacion impecable.",
    priceRange: "high",
    priceLabel: "$$$",
    eventTypes: ["bodas", "corporativos"],
    image: "linear-gradient(135deg, #4a7c9b 0%, #3a6480 100%)",
    badge: "Top Rated",
    badgeClass: "premium",
    services: [
      { name: "Menu Completo (por persona)", price: "$45 - $85" },
      { name: "Cocteleria", price: "$25 - $40" },
      { name: "Brunch", price: "$30 - $50" }
    ],
    gallery: 6,
    verified: true
  },
  {
    id: 2,
    name: "Studio Moments",
    category: "fotografia",
    categoryLabel: "Fotografia y Video",
    location: "santo-domingo",
    locationLabel: "Santo Domingo",
    rating: 4.8,
    reviews: 189,
    description: "Capturamos los momentos mas preciados de tu vida. Fotografia artistica y video cinematografico para bodas y eventos especiales.",
    priceRange: "medium",
    priceLabel: "$$",
    eventTypes: ["bodas", "quinceañeras"],
    image: "linear-gradient(135deg, #c9a962 0%, #a88b4a 100%)",
    badge: "Verificado",
    badgeClass: "verified",
    services: [
      { name: "Cobertura Completa (8hrs)", price: "$1,200" },
      { name: "Video + Foto", price: "$2,000" },
      { name: "Album Premium", price: "$350" }
    ],
    gallery: 8,
    verified: true
  },
  {
    id: 3,
    name: "Ritmo Latino DJ",
    category: "musica",
    categoryLabel: "Musica",
    location: "punta-cana",
    locationLabel: "Punta Cana",
    rating: 4.9,
    reviews: 312,
    description: "La mejor musica para mantener la fiesta toda la noche. DJ profesional con equipo de ultima generacion.",
    priceRange: "medium",
    priceLabel: "$$",
    eventTypes: ["bodas", "cumpleanos"],
    image: "linear-gradient(135deg, #6b9bb8 0%, #4a7c9b 100%)",
    badge: "Premium",
    badgeClass: "premium",
    services: [
      { name: "DJ + Equipo (6hrs)", price: "$800" },
      { name: "Iluminacion LED", price: "$300" },
      { name: "Karaoke", price: "$150" }
    ],
    gallery: 5,
    verified: true
  },
  {
    id: 4,
    name: "Flores del Caribe",
    category: "decoracion",
    categoryLabel: "Decoracion y Flores",
    location: "santiago",
    locationLabel: "Santiago",
    rating: 4.7,
    reviews: 156,
    description: "Transformamos espacios en ambientes magicos. Arreglos florales, decoracion tematica y ambientacion para todo tipo de eventos.",
    priceRange: "medium",
    priceLabel: "$$",
    eventTypes: ["bodas", "baby-shower"],
    image: "linear-gradient(135deg, #e8a5a5 0%, #d08080 100%)",
    badge: "Verificado",
    badgeClass: "verified",
    services: [
      { name: "Decoracion Completa", price: "Desde $500" },
      { name: "Centro de Mesa", price: "$35 - $75" },
      { name: "Arco Floral", price: "$250" }
    ],
    gallery: 10,
    verified: true
  },
  {
    id: 5,
    name: "Hacienda Los Pinos",
    category: "venue",
    categoryLabel: "Salones y Venues",
    location: "la-romana",
    locationLabel: "La Romana",
    rating: 4.8,
    reviews: 98,
    description: "Hermoso salon campestre con capacidad para 300 personas. Jardines, piscina y estacionamiento amplio incluido.",
    priceRange: "high",
    priceLabel: "$$$",
    eventTypes: ["bodas", "corporativos"],
    image: "linear-gradient(135deg, #8fbc8f 0%, #6b9b6b 100%)",
    badge: "Premium",
    badgeClass: "premium",
    services: [
      { name: "Alquiler Salon (8hrs)", price: "$2,500" },
      { name: "Jardin Exterior", price: "$1,800" },
      { name: "Paquete Completo", price: "$4,000" }
    ],
    gallery: 12,
    verified: true
  },
  {
    id: 6,
    name: "Party Rentals RD",
    category: "alquiler",
    categoryLabel: "Alquiler de Equipos",
    location: "santo-domingo",
    locationLabel: "Santo Domingo",
    rating: 4.6,
    reviews: 203,
    description: "Todo lo que necesitas para tu evento: sillas, mesas, carpas, iluminacion y mas. Entrega e instalacion incluida.",
    priceRange: "low",
    priceLabel: "$",
    eventTypes: ["bodas", "cumpleanos", "corporativos"],
    image: "linear-gradient(135deg, #a0aec0 0%, #718096 100%)",
    badge: "",
    badgeClass: "",
    services: [
      { name: "Silla Tiffany", price: "$5/unidad" },
      { name: "Mesa Redonda", price: "$15/unidad" },
      { name: "Carpa 10x10", price: "$350" }
    ],
    gallery: 6,
    verified: false
  }
];

// DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  initializeApp();
});

// Initialize Application
function initializeApp() {
  loadSuppliers();
  checkAuthStatus();
  setupEventListeners();
}

// Setup Event Listeners
function setupEventListeners() {
  // Close dropdowns when clicking outside
  document.addEventListener('click', (e) => {
    const userMenu = document.querySelector('.user-menu');
    if (userMenu && !userMenu.contains(e.target)) {
      userMenu.classList.remove('active');
    }
  });

  // Escape key to close modals
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal.active').forEach(modal => {
        closeModal(modal.id);
      });
    }
  });
}

// Page Navigation
function showPage(pageId) {
  // Hide all pages
  document.querySelectorAll('.page').forEach(page => {
    page.classList.remove('active');
  });

  // Show requested page
  const page = document.getElementById(pageId + 'Page');
  if (page) {
    page.classList.add('active');
    window.scrollTo(0, 0);

    // Load page-specific content
    switch(pageId) {
      case 'home':
        break;
      case 'explore':
        loadExploreSuppliers();
        break;
      case 'dashboard':
        loadDashboard();
        break;
      case 'bookings':
        loadBookings();
        break;
      case 'messages':
        loadMessages();
        break;
      case 'profile':
        loadProfileForm();
        break;
    }
  }
}

// Load Suppliers (Home Page)
function loadSuppliers() {
  const grid = document.getElementById('suppliersGrid');
  if (!grid) return;

  const featuredSuppliers = suppliers.slice(0, 3);
  grid.innerHTML = featuredSuppliers.map(supplier => createSupplierCard(supplier)).join('');
}

// Create Supplier Card HTML
function createSupplierCard(supplier) {
  return `
    <div class="supplier-card" onclick="viewSupplierProfile(${supplier.id})">
      <div class="supplier-image" style="background: ${supplier.image};">
        ${supplier.badge ? `<span class="supplier-badge ${supplier.badgeClass}">${supplier.badge}</span>` : ''}
      </div>
      <div class="supplier-info">
        <h3>${supplier.name}</h3>
        <span class="supplier-category">${supplier.categoryLabel}</span>
        <div class="supplier-rating">
          <span class="stars">${'&#9733;'.repeat(Math.floor(supplier.rating))}${supplier.rating % 1 >= 0.5 ? '&#9733;' : ''}</span>
          <span class="rating-text">${supplier.rating} (${supplier.reviews} resenas)</span>
        </div>
        <p class="supplier-description">${supplier.description}</p>
        <div class="supplier-tags">
          ${supplier.eventTypes.slice(0, 2).map(type => `<span class="tag">${capitalizeFirst(type)}</span>`).join('')}
        </div>
        <div class="supplier-price">
          <span class="price-label">Desde</span>
          <span class="price-value">${supplier.priceLabel}</span>
        </div>
      </div>
    </div>
  `;
}

// Load Explore Page Suppliers
function loadExploreSuppliers() {
  applyFilters();
}

// Apply Filters
function applyFilters() {
  const category = document.getElementById('filterCategory')?.value || '';
  const location = document.getElementById('filterLocation')?.value || '';
  const price = document.getElementById('filterPrice')?.value || '';
  const rating = document.getElementById('filterRating')?.value || '';
  const eventType = document.getElementById('filterEventType')?.value || '';

  let filtered = suppliers.filter(supplier => {
    if (category && supplier.category !== category) return false;
    if (location && supplier.location !== location) return false;
    if (price && supplier.priceRange !== price) return false;
    if (rating && supplier.rating < parseFloat(rating)) return false;
    if (eventType && !supplier.eventTypes.includes(eventType)) return false;
    return true;
  });

  const grid = document.getElementById('exploreGrid');
  if (grid) {
    if (filtered.length > 0) {
      grid.innerHTML = filtered.map(supplier => createSupplierCard(supplier)).join('');
    } else {
      grid.innerHTML = `
        <div class="empty-state large" style="grid-column: 1 / -1;">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <h3>No se encontraron suplidores</h3>
          <p>Intenta ajustar los filtros de busqueda</p>
          <button class="btn btn-primary" onclick="clearFilters()">Limpiar Filtros</button>
        </div>
      `;
    }
  }

  const resultsCount = document.getElementById('resultsCount');
  if (resultsCount) {
    resultsCount.textContent = `Mostrando ${filtered.length} suplidores`;
  }
}

// Clear Filters
function clearFilters() {
  document.getElementById('filterCategory').value = '';
  document.getElementById('filterLocation').value = '';
  document.getElementById('filterPrice').value = '';
  document.getElementById('filterRating').value = '';
  document.getElementById('filterEventType').value = '';
  applyFilters();
}

// Filter by Category (from home page)
function filterByCategory(category) {
  showPage('explore');
  setTimeout(() => {
    document.getElementById('filterCategory').value = category;
    applyFilters();
  }, 100);
}

// Search Suppliers
function searchSuppliers() {
  const query = document.getElementById('searchInput')?.value || '';
  const category = document.getElementById('categorySelect')?.value || '';

  showPage('explore');
  
  setTimeout(() => {
    if (category) {
      document.getElementById('filterCategory').value = category;
    }
    applyFilters();
    
    if (query) {
      showToast(`Buscando: "${query}"`, 'success');
    }
  }, 100);
}

// View Supplier Profile
function viewSupplierProfile(supplierId) {
  currentSupplier = suppliers.find(s => s.id === supplierId);
  if (!currentSupplier) return;

  showPage('supplierProfile');
  
  // Load profile header
  const header = document.getElementById('supplierProfileHeader');
  header.innerHTML = `
    <div class="profile-avatar">${currentSupplier.name.charAt(0)}</div>
    <div class="profile-title">
      <h1>${currentSupplier.name}</h1>
      <span class="category">${currentSupplier.categoryLabel}</span>
      <div class="profile-meta">
        <div class="profile-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          ${currentSupplier.locationLabel}
        </div>
        <div class="profile-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26 12,2"/></svg>
          ${currentSupplier.rating} (${currentSupplier.reviews} resenas)
        </div>
        <div class="profile-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22,4 12,14.01 9,11.01"/></svg>
          ${currentSupplier.verified ? 'Verificado' : 'No verificado'}
        </div>
      </div>
    </div>
    <div class="profile-actions">
      <button class="btn btn-white" onclick="requestBooking(${currentSupplier.id})">Solicitar Reserva</button>
      <button class="btn btn-outline-white" onclick="messageSupplier(${currentSupplier.id})">Enviar Mensaje</button>
    </div>
  `;

  // Load profile main content
  const main = document.getElementById('supplierProfileMain');
  main.innerHTML = `
    <div class="profile-card">
      <h3>Acerca de</h3>
      <p>${currentSupplier.description}</p>
    </div>
    <div class="profile-card">
      <h3>Galeria</h3>
      <div class="gallery-grid">
        ${Array(currentSupplier.gallery).fill().map((_, i) => `
          <div class="gallery-item" style="background: ${currentSupplier.image};"></div>
        `).join('')}
      </div>
    </div>
    <div class="profile-card">
      <h3>Servicios y Precios</h3>
      <div class="services-list">
        ${currentSupplier.services.map(service => `
          <div class="service-item">
            <span class="service-name">${service.name}</span>
            <span class="service-price">${service.price}</span>
          </div>
        `).join('')}
      </div>
    </div>
    <div class="profile-card">
      <h3>Resenas</h3>
      <div class="reviews-list">
        <div class="review-item">
          <div class="review-header">
            <span class="reviewer-name">Maria Garcia</span>
            <span class="review-date">Hace 2 semanas</span>
          </div>
          <div class="review-stars">${'&#9733;'.repeat(5)}</div>
          <p class="review-text">Excelente servicio! Todo salio perfecto en mi boda. Super recomendados.</p>
        </div>
        <div class="review-item">
          <div class="review-header">
            <span class="reviewer-name">Carlos Rodriguez</span>
            <span class="review-date">Hace 1 mes</span>
          </div>
          <div class="review-stars">${'&#9733;'.repeat(5)}</div>
          <p class="review-text">Muy profesionales y puntuales. El resultado supero nuestras expectativas.</p>
        </div>
      </div>
    </div>
  `;

  // Load profile sidebar
  const sidebar = document.getElementById('supplierProfileSidebar');
  sidebar.innerHTML = `
    <div class="sidebar-card">
      <h4>Estadisticas</h4>
      <div class="quick-stats">
        <div class="quick-stat">
          <span class="quick-stat-value">${currentSupplier.reviews}</span>
          <span class="quick-stat-label">Resenas</span>
        </div>
        <div class="quick-stat">
          <span class="quick-stat-value">${currentSupplier.rating}</span>
          <span class="quick-stat-label">Calificacion</span>
        </div>
        <div class="quick-stat">
          <span class="quick-stat-value">98%</span>
          <span class="quick-stat-label">Respuesta</span>
        </div>
        <div class="quick-stat">
          <span class="quick-stat-value">< 2hrs</span>
          <span class="quick-stat-label">Tiempo Resp.</span>
        </div>
      </div>
    </div>
    <div class="sidebar-card">
      <h4>Disponibilidad</h4>
      <div class="availability-calendar">
        ${['L', 'M', 'X', 'J', 'V', 'S', 'D'].map(day => `<div class="calendar-day">${day}</div>`).join('')}
        ${Array(28).fill().map(() => `<div class="calendar-day ${Math.random() > 0.3 ? 'available' : 'unavailable'}"></div>`).join('')}
      </div>
    </div>
    <div class="sidebar-card">
      <h4>Tipos de Evento</h4>
      <div class="supplier-tags" style="flex-wrap: wrap; gap: 8px; display: flex;">
        ${currentSupplier.eventTypes.map(type => `<span class="tag">${capitalizeFirst(type)}</span>`).join('')}
      </div>
    </div>
  `;
}

// Request Booking
function requestBooking(supplierId) {
  if (!currentUser) {
    showToast('Debes iniciar sesion para reservar', 'warning');
    openModal('loginModal');
    return;
  }

  const supplier = suppliers.find(s => s.id === supplierId);
  if (!supplier) return;

  const bookingInfo = document.getElementById('bookingSupplierInfo');
  bookingInfo.innerHTML = `
    <div class="booking-supplier-avatar">${supplier.name.charAt(0)}</div>
    <div class="booking-supplier-details">
      <h4>${supplier.name}</h4>
      <p>${supplier.categoryLabel} - ${supplier.locationLabel}</p>
    </div>
  `;

  openModal('bookingModal');
}

// Submit Booking
function submitBooking(e) {
  e.preventDefault();

  const booking = {
    date: document.getElementById('bookingDate').value,
    time: document.getElementById('bookingTime').value,
    eventType: document.getElementById('bookingEventType').value,
    guests: document.getElementById('bookingGuests').value,
    location: document.getElementById('bookingLocation').value,
    details: document.getElementById('bookingDetails').value,
    budget: document.getElementById('bookingBudget').value,
    supplierId: currentSupplier?.id,
    status: 'pending'
  };

  // Save booking (simulated)
  const bookings = JSON.parse(localStorage.getItem('bookings') || '[]');
  bookings.push({ ...booking, id: Date.now(), createdAt: new Date().toISOString() });
  localStorage.setItem('bookings', JSON.stringify(bookings));

  closeModal('bookingModal');
  showToast('Solicitud de reserva enviada exitosamente!', 'success');
  document.getElementById('bookingForm').reset();
}

// Message Supplier
function messageSupplier(supplierId) {
  if (!currentUser) {
    showToast('Debes iniciar sesion para enviar mensajes', 'warning');
    openModal('loginModal');
    return;
  }

  const supplier = suppliers.find(s => s.id === supplierId);
  if (!supplier) return;

  const recipient = document.getElementById('messageRecipient');
  recipient.innerHTML = `
    <div class="message-recipient-avatar">${supplier.name.charAt(0)}</div>
    <div class="message-recipient-info">
      <h4>${supplier.name}</h4>
      <p>${supplier.categoryLabel}</p>
    </div>
  `;

  openModal('messageSupplierModal');
}

// Send Message
function sendMessage(e) {
  e.preventDefault();

  const message = {
    subject: document.getElementById('messageSubject').value,
    content: document.getElementById('messageContent').value,
    supplierId: currentSupplier?.id,
    createdAt: new Date().toISOString()
  };

  // Save message (simulated)
  const messages = JSON.parse(localStorage.getItem('messages') || '[]');
  messages.push({ ...message, id: Date.now() });
  localStorage.setItem('messages', JSON.stringify(messages));

  closeModal('messageSupplierModal');
  showToast('Mensaje enviado exitosamente!', 'success');
  document.getElementById('messageSupplierForm').reset();
}

// Load Dashboard
function loadDashboard() {
  if (!currentUser) return;

  const welcomeEl = document.getElementById('dashboardWelcome');
  if (welcomeEl) {
    welcomeEl.textContent = `Bienvenido de vuelta, ${currentUser.firstName}`;
  }

  if (currentUser.type === 'client') {
    document.getElementById('clientDashboard').style.display = 'block';
    document.getElementById('supplierDashboard').style.display = 'none';
    loadClientDashboard();
  } else {
    document.getElementById('clientDashboard').style.display = 'none';
    document.getElementById('supplierDashboard').style.display = 'block';
    loadSupplierDashboard();
  }
}

// Load Client Dashboard
function loadClientDashboard() {
  const bookings = JSON.parse(localStorage.getItem('bookings') || '[]');
  const messages = JSON.parse(localStorage.getItem('messages') || '[]');

  document.getElementById('clientEventsCount').textContent = bookings.filter(b => b.status === 'pending' || b.status === 'confirmed').length;
  document.getElementById('clientBookingsCount').textContent = bookings.filter(b => b.status === 'completed').length;
  document.getElementById('clientMessagesCount').textContent = messages.length;
  document.getElementById('clientFavoritesCount').textContent = '0';

  // Load recommended suppliers
  const recommended = document.getElementById('recommendedSuppliers');
  if (recommended) {
    recommended.innerHTML = suppliers.slice(0, 4).map(supplier => `
      <div class="recommended-card" onclick="viewSupplierProfile(${supplier.id})">
        <div class="recommended-avatar">${supplier.name.charAt(0)}</div>
        <h4>${supplier.name}</h4>
        <p>${supplier.categoryLabel}</p>
      </div>
    `).join('');
  }
}

// Load Supplier Dashboard
function loadSupplierDashboard() {
  document.getElementById('supplierViewsCount').textContent = Math.floor(Math.random() * 500) + 100;
  document.getElementById('supplierEarningsCount').textContent = '$' + (Math.floor(Math.random() * 5000) + 1000).toLocaleString();
  document.getElementById('supplierBookingsCount').textContent = Math.floor(Math.random() * 10);
  document.getElementById('supplierRatingValue').textContent = (Math.random() * 0.5 + 4.5).toFixed(1);
}

// Load Bookings
function loadBookings() {
  const bookings = JSON.parse(localStorage.getItem('bookings') || '[]');
  const container = document.getElementById('bookingsContainer');

  if (bookings.length === 0) {
    container.innerHTML = `
      <div class="empty-state large">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
        <h3>No tienes reservas</h3>
        <p>Cuando realices una reserva, aparecera aqui</p>
        <button class="btn btn-primary" onclick="showPage('explore')">Explorar Suplidores</button>
      </div>
    `;
  } else {
    container.innerHTML = bookings.map(booking => {
      const supplier = suppliers.find(s => s.id === booking.supplierId) || { name: 'Suplidor', categoryLabel: 'Servicio' };
      return `
        <div class="booking-card">
          <div class="booking-avatar">${supplier.name.charAt(0)}</div>
          <div class="booking-info">
            <h4>${supplier.name}</h4>
            <div class="booking-details">
              <span class="booking-detail">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                ${booking.date}
              </span>
              <span class="booking-detail">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                ${booking.time}
              </span>
              <span class="booking-detail">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                ${booking.guests} personas
              </span>
            </div>
          </div>
          <div class="booking-status">
            <span class="status-badge ${booking.status}">${getStatusLabel(booking.status)}</span>
            <button class="btn btn-small btn-secondary" onclick="viewBookingDetails(${booking.id})">Ver Detalles</button>
          </div>
        </div>
      `;
    }).join('');
  }
}

// Filter Bookings
function filterBookings(status) {
  document.querySelectorAll('.tabs .tab').forEach(tab => tab.classList.remove('active'));
  event.target.classList.add('active');
  // Implement filtering logic
  loadBookings();
}

// Get Status Label
function getStatusLabel(status) {
  const labels = {
    pending: 'Pendiente',
    confirmed: 'Confirmada',
    completed: 'Completada',
    cancelled: 'Cancelada'
  };
  return labels[status] || status;
}

// Load Messages
function loadMessages() {
  const messages = JSON.parse(localStorage.getItem('messages') || '[]');
  const container = document.getElementById('conversationsItems');

  if (messages.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <p>No tienes conversaciones</p>
      </div>
    `;
  } else {
    container.innerHTML = messages.map(msg => {
      const supplier = suppliers.find(s => s.id === msg.supplierId) || { name: 'Suplidor' };
      return `
        <div class="conversation-item" onclick="openConversation(${msg.id})">
          <div class="conversation-avatar">${supplier.name.charAt(0)}</div>
          <div class="conversation-info">
            <div class="conversation-name">${supplier.name}</div>
            <div class="conversation-preview">${msg.subject}</div>
          </div>
          <div class="conversation-time">${formatDate(msg.createdAt)}</div>
        </div>
      `;
    }).join('');
  }
}

// Load Profile Form
function loadProfileForm() {
  if (!currentUser) return;

  document.getElementById('profileFirstName').value = currentUser.firstName || '';
  document.getElementById('profileLastName').value = currentUser.lastName || '';
  document.getElementById('profileEmail').value = currentUser.email || '';
  document.getElementById('profilePhone').value = currentUser.phone || '';
  document.getElementById('profileLocation').value = currentUser.location || '';
  document.getElementById('profileAvatar').textContent = currentUser.firstName?.charAt(0) || 'U';
}

// Save Profile
function saveProfile(e) {
  e.preventDefault();

  currentUser.firstName = document.getElementById('profileFirstName').value;
  currentUser.lastName = document.getElementById('profileLastName').value;
  currentUser.email = document.getElementById('profileEmail').value;
  currentUser.phone = document.getElementById('profilePhone').value;
  currentUser.location = document.getElementById('profileLocation').value;

  localStorage.setItem('currentUser', JSON.stringify(currentUser));
  showToast('Perfil actualizado exitosamente!', 'success');
  updateUserUI();
}

// Show Profile Section
function showProfileSection(section) {
  document.querySelectorAll('.profile-menu a').forEach(a => a.classList.remove('active'));
  event.target.classList.add('active');
  // Implement section switching
}

// Authentication
function checkAuthStatus() {
  const savedUser = localStorage.getItem('currentUser');
  if (savedUser) {
    currentUser = JSON.parse(savedUser);
    updateUserUI();
  }
}

function updateUserUI() {
  const navButtons = document.getElementById('navButtons');
  const navUser = document.getElementById('navUser');

  if (currentUser) {
    navButtons.style.display = 'none';
    navUser.style.display = 'block';
    document.getElementById('userAvatar').textContent = currentUser.firstName?.charAt(0) || 'U';
    document.getElementById('userName').textContent = currentUser.firstName || 'Usuario';
  } else {
    navButtons.style.display = 'flex';
    navUser.style.display = 'none';
  }
}

function toggleUserMenu() {
  document.querySelector('.user-menu').classList.toggle('active');
}

// Handle Login
function handleLogin(e) {
  e.preventDefault();

  const email = document.getElementById('loginEmail').value;
  const password = document.getElementById('loginPassword').value;

  // Simulated login
  const users = JSON.parse(localStorage.getItem('users') || '[]');
  const user = users.find(u => u.email === email);

  if (user && user.password === password) {
    currentUser = user;
    localStorage.setItem('currentUser', JSON.stringify(currentUser));
    closeModal('loginModal');
    updateUserUI();
    showToast('Bienvenido de vuelta!', 'success');
    showPage('dashboard');
  } else {
    showToast('Credenciales incorrectas', 'error');
  }
}

// Handle Register
function handleRegister(e) {
  e.preventDefault();

  const password = document.getElementById('registerPassword').value;
  const confirmPassword = document.getElementById('registerPasswordConfirm').value;

  if (password !== confirmPassword) {
    showToast('Las contrasenas no coinciden', 'error');
    return;
  }

  if (password.length < 8) {
    showToast('La contrasena debe tener al menos 8 caracteres', 'error');
    return;
  }

  const newUser = {
    id: Date.now(),
    firstName: document.getElementById('registerFirstName').value,
    lastName: document.getElementById('registerLastName').value,
    email: document.getElementById('registerEmail').value,
    phone: document.getElementById('registerPhone').value,
    password: password,
    type: selectedUserType,
    plan: selectedPlan,
    createdAt: new Date().toISOString()
  };

  if (selectedUserType === 'supplier') {
    newUser.businessName = document.getElementById('registerBusinessName').value;
    newUser.category = document.getElementById('registerCategory').value;
    newUser.location = document.getElementById('registerLocation').value;
  }

  // Save user
  const users = JSON.parse(localStorage.getItem('users') || '[]');
  users.push(newUser);
  localStorage.setItem('users', JSON.stringify(users));

  // Auto login
  currentUser = newUser;
  localStorage.setItem('currentUser', JSON.stringify(currentUser));

  closeModal('registerModal');
  resetRegisterForm();
  updateUserUI();
  showToast('Cuenta creada exitosamente!', 'success');
  showPage('dashboard');
}

// Logout
function logout() {
  currentUser = null;
  localStorage.removeItem('currentUser');
  updateUserUI();
  showPage('home');
  showToast('Sesion cerrada', 'success');
}

// Register Steps
function selectUserType(type) {
  selectedUserType = type;
  
  document.querySelectorAll('.user-type-card').forEach(card => card.classList.remove('selected'));
  event?.target?.closest('.user-type-card')?.classList.add('selected');

  setTimeout(() => {
    if (type === 'client') {
      goToRegisterStep(3);
      document.getElementById('supplierFields').style.display = 'none';
      document.getElementById('registerFormTitle').textContent = 'Completa tu registro como Cliente';
    } else {
      goToRegisterStep(2);
    }
  }, 200);
}

function selectPlan(plan) {
  selectedPlan = plan;
  
  setTimeout(() => {
    goToRegisterStep(3);
    document.getElementById('supplierFields').style.display = 'block';
    document.getElementById('registerFormTitle').textContent = 'Completa tu registro como Proveedor';
  }, 200);
}

function goToRegisterStep(step) {
  document.querySelectorAll('.register-step').forEach(s => s.classList.remove('active'));
  document.getElementById(`registerStep${step}`).classList.add('active');
}

function resetRegisterForm() {
  selectedUserType = null;
  selectedPlan = null;
  goToRegisterStep(1);
  document.getElementById('registerForm').reset();
}

// Modal Functions
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// Mobile Menu
function toggleMobileMenu() {
  const btn = document.querySelector('.mobile-menu-btn');
  const navLinks = document.getElementById('navLinks');
  const navButtons = document.getElementById('navButtons');
  
  btn.classList.toggle('active');
  navLinks.classList.toggle('active');
  
  if (!currentUser) {
    navButtons.classList.toggle('active');
  }
}

// Toast Notifications
function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  
  container.appendChild(toast);
  
  setTimeout(() => toast.classList.add('show'), 10);
  
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// Contact Form
function submitContactForm(e) {
  e.preventDefault();
  showToast('Mensaje enviado exitosamente!', 'success');
  document.getElementById('contactForm').reset();
}

// Chat Functions
let chatStep = 0;

const chatFlow = {
  catering: {
    message: "Excelente eleccion! Para cuantas personas necesitas el servicio de catering?",
    options: ["Menos de 50", "50 - 100", "100 - 200", "Mas de 200"]
  },
  fotografia: {
    message: "Perfecto! Que tipo de evento necesitas fotografiar?",
    options: ["Boda", "Cumpleanos", "Corporativo", "Quinceañera"]
  },
  musica: {
    message: "Genial! Que estilo de musica o entretenimiento buscas?",
    options: ["DJ", "Banda en Vivo", "Mariachi", "Animador"]
  },
  decoracion: {
    message: "Hermoso! Que estilo de decoracion tienes en mente?",
    options: ["Elegante/Clasico", "Moderno/Minimalista", "Rustico", "Tematico"]
  },
  otro: {
    message: "Cuentame mas sobre lo que necesitas para tu evento.",
    options: ["Salon/Venue", "Alquiler de Equipos", "Planificacion Completa", "Otros"]
  },
  default: {
    message: "Gracias! Un asesor se pondra en contacto contigo pronto. Deseas dejarnos tu numero de contacto?",
    options: ["Si, contactarme", "Prefiero email", "Mas tarde"]
  },
  final: {
    message: "Perfecto! Hemos registrado tu solicitud. Nos comunicaremos contigo dentro de las proximas 24 horas. Gracias por elegir Premier Services!",
    options: []
  }
};

function handleChatOption(btn) {
  const response = btn.dataset.response || btn.textContent;
  
  // Add user message
  addChatMessage(btn.textContent, false);
  
  chatStep++;
  
  let nextFlow;
  if (chatStep === 1) {
    nextFlow = chatFlow[response] || chatFlow.default;
  } else if (chatStep === 2) {
    nextFlow = chatFlow.default;
  } else {
    nextFlow = chatFlow.final;
  }
  
  setTimeout(() => {
    addChatMessage(nextFlow.message, true);
    updateChatOptions(nextFlow.options);
  }, 500);
}

function addChatMessage(text, isBot) {
  const messages = document.getElementById('chatMessages');
  const div = document.createElement('div');
  div.className = `message ${isBot ? 'bot' : 'user'}`;
  div.innerHTML = `<p>${text}</p>`;
  messages.appendChild(div);
  messages.scrollTop = messages.scrollHeight;
}

function updateChatOptions(options) {
  const container = document.getElementById('chatOptions');
  
  if (options.length === 0) {
    container.innerHTML = `<button class="btn btn-primary btn-full" onclick="closeModal('chatModal'); resetChat();">Cerrar</button>`;
  } else {
    container.innerHTML = options.map(opt => `
      <button class="chat-option" onclick="handleChatOption(this)">${opt}</button>
    `).join('');
  }
}

function resetChat() {
  chatStep = 0;
  const messages = document.getElementById('chatMessages');
  messages.innerHTML = `
    <div class="message bot">
      <p>Hola! Bienvenido a Premier Services. Que tipo de servicio estas buscando?</p>
    </div>
  `;
  updateChatOptions(['Catering', 'Fotografia', 'Musica', 'Decoracion', 'Otro']);
}

// FAQ Toggle
function toggleFaq(btn) {
  const item = btn.closest('.faq-item');
  item.classList.toggle('active');
}

// Utility Functions
function capitalizeFirst(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function formatDate(dateStr) {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 86400000) return 'Hoy';
  if (diff < 172800000) return 'Ayer';
  if (diff < 604800000) return `Hace ${Math.floor(diff / 86400000)} dias`;
  
  return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
}

// Initialize chat options on modal open
document.addEventListener('click', (e) => {
  if (e.target.closest('[onclick*="chatModal"]')) {
    setTimeout(resetChat, 100);
  }
});
