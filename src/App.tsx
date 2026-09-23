import React, { useState, useEffect } from 'react';
import { Navigation, PageView } from './components/Navigation';
import { DashboardPage } from './pages/DashboardPage';
import { InventoryPage } from './pages/InventoryPage';
import { FoodFormPage } from './pages/FoodFormPage';
import { RecipesPage } from './pages/RecipesPage';
import { ImpactPage } from './pages/ImpactPage';
import { MarketplacePage } from './pages/MarketplacePage';
import { FoodItem, User } from './types/api';
import { AlertCircleIcon } from './components/Icons';
import { api } from './api/endpoints';

const DEFAULT_USERS: User[] = [
  { id: '11111111-1111-1111-1111-111111111111', name: 'Favian' },
  { id: '22222222-2222-2222-2222-222222222222', name: 'Ana' },
];

export const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<PageView>('dashboard');
  const [editingItem, setEditingItem] = useState<FoodItem | null>(null);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [users, setUsers] = useState<User[]>(DEFAULT_USERS);
  const [currentUser, setCurrentUser] = useState<User>(DEFAULT_USERS[0]);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    api.getUsers()
      .then((data: any[]) => {
        if (data && data.length > 0) {
          const normalized = data.map((u) => ({
            id: u.id || u.userId,
            name: u.name || u.username,
          }));
          setUsers(normalized);
          setCurrentUser(normalized[0]);
        }
      })
      .catch(() => {});

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const handleNavigate = (view: PageView, itemToEdit?: FoodItem) => {
    setEditingItem(view === 'create-food' && itemToEdit ? itemToEdit : null);
    setCurrentView(view);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="viewport-shell">
      <header className="glass-topbar">
        <div className="topbar-constraint">
          <div className="brand-cluster" onClick={() => handleNavigate('dashboard')}>
            <span className="brand-dot" />
            <span className="brand-name">sobra</span>
          </div>

          <div className="household-selector">
            {/* Icono de usuario vectorial en línea (sin dependencias ni emojis) */}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '6px', verticalAlign: 'middle' }}>
              <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <select
              value={currentUser.id}
              onChange={(e) => {
                const found = users.find((u) => u.id === e.target.value);
                if (found) setCurrentUser(found);
              }}
              className="user-switch-select"
              aria-label="Cambiar usuario de demostración"
            >
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </header>

      {!isOnline && (
        <div className="offline-pill-bar" role="status">
          <AlertCircleIcon size={14} />
          <span>Sin conexión: Los registros requieren enlace activo con el servidor.</span>
        </div>
      )}

      <main className="screen-content">
        {currentView === 'dashboard' && (
          <DashboardPage currentUser={currentUser} onNavigate={handleNavigate} />
        )}
        {currentView === 'inventory' && (
          <InventoryPage currentUser={currentUser} onNavigate={handleNavigate} />
        )}
        {currentView === 'create-food' && (
          <FoodFormPage currentUser={currentUser} editingItem={editingItem} onNavigate={handleNavigate} />
        )}
        {currentView === 'marketplace' && (
          <MarketplacePage currentUser={currentUser} onNavigate={handleNavigate} />
        )}
        {currentView === 'recipes' && <RecipesPage onNavigate={handleNavigate} />}
        {currentView === 'impact' && <ImpactPage />}
      </main>

      <Navigation currentView={currentView} onNavigate={handleNavigate} />
    </div>
  );
};