import React from 'react';
import { HomeIcon, BoxIcon, PlusIcon, ChefHatIcon, StoreIcon } from './Icons';

export type PageView = 'dashboard' | 'inventory' | 'create-food' | 'marketplace' | 'recipes' | 'impact';

interface Props {
  currentView: PageView;
  onNavigate: (view: PageView) => void;
}

export const Navigation: React.FC<Props> = ({ currentView, onNavigate }) => {
  return (
    <div className="bottom-nav-wrapper">
      <nav className="bottom-nav">
        <button
          className={`nav-tab ${currentView === 'dashboard' ? 'active' : ''}`}
          onClick={() => onNavigate('dashboard')}
          aria-label="Inicio"
        >
          <span className="nav-icon"><HomeIcon size={19} /></span>
          <span className="nav-label">Inicio</span>
        </button>

        <button
          className={`nav-tab ${currentView === 'inventory' ? 'active' : ''}`}
          onClick={() => onNavigate('inventory')}
          aria-label="Inventario"
        >
          <span className="nav-icon"><BoxIcon size={19} /></span>
          <span className="nav-label">Despensa</span>
        </button>

        <button
          className="nav-action-pill"
          onClick={() => onNavigate('create-food')}
          aria-label="Registrar alimento"
        >
          <PlusIcon size={20} />
        </button>

        <button
          className={`nav-tab ${currentView === 'marketplace' ? 'active' : ''}`}
          onClick={() => onNavigate('marketplace')}
          aria-label="Comunidad"
        >
          <span className="nav-icon"><StoreIcon size={19} /></span>
          <span className="nav-label">Comunidad</span>
        </button>

        <button
          className={`nav-tab ${currentView === 'recipes' ? 'active' : ''}`}
          onClick={() => onNavigate('recipes')}
          aria-label="Recetas"
        >
          <span className="nav-icon"><ChefHatIcon size={19} /></span>
          <span className="nav-label">Recetas</span>
        </button>
      </nav>
    </div>
  );
};
