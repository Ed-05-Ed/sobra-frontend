import React, { useState, useEffect } from 'react';
import { RecipeSuggestion } from '../types/api';
import { api } from '../api/endpoints';
import { SparklesIcon, CheckCircleIcon, AlertCircleIcon, ChefHatIcon } from '../components/Icons';

interface Props {
  onNavigate: (view: any) => void;
}

export const RecipesPage: React.FC<Props> = ({ onNavigate }) => {
  const [recipes, setRecipes] = useState<RecipeSuggestion[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeRecipeId, setActiveRecipeId] = useState<string | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await api.getSuggestions();
      setRecipes(data);
    } catch (err: any) {
      setError(err.message || 'Error al obtener recetas recomendadas.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const renderInstructions = (instructions: string | string[]) => {
    if (Array.isArray(instructions)) {
      return (
        <ol className="step-by-step">
          {instructions.map((step, idx) => (
            <li key={idx}>
              <span className="step-num">{idx + 1}</span>
              <p className="step-text">{step}</p>
            </li>
          ))}
        </ol>
      );
    }

    // Si viene como un string largo con saltos de línea o párrafos
    const steps = instructions.split('\n').filter((s) => s.trim().length > 0);
    return (
      <ol className="step-by-step">
        {steps.map((step, idx) => (
          <li key={idx}>
            <span className="step-num">{idx + 1}</span>
            <p className="step-text">{step}</p>
          </li>
        ))}
      </ol>
    );
  };

  return (
    <div className="view-wrapper">
      <header className="page-nav-header">
        <div>
          <span className="hero-tag">RECOMENDACIONES</span>
          <h1 className="hero-headline">Ideas con lo que tienes</h1>
        </div>
        <button className="btn-icon-accent" onClick={loadData} title="Refrescar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" />
          </svg>
        </button>
      </header>

      {error && (
        <div className="alert-banner error" role="alert">
          <AlertCircleIcon size={16} />
          <span>{error}</span>
          <button className="btn-link" onClick={loadData}>Reintentar</button>
        </div>
      )}

      {isLoading ? (
        <div className="skeleton-list">
          <div className="skeleton-card" />
          <div className="skeleton-card" />
        </div>
      ) : recipes.length === 0 ? (
        <div className="clean-empty-box">
          <div className="clean-empty-icon"><ChefHatIcon size={26} /></div>
          <h4>Sin recetas sugeridas</h4>
          <p>No encontramos combinaciones que coincidan con los ingredientes activos.</p>
          <button className="btn-primary-sm" onClick={() => onNavigate('inventory')}>
            Explorar despensa
          </button>
        </div>
      ) : (
        <div className="recipe-cards-stack">
          {recipes.map((recipe) => {
            const isExpanded = activeRecipeId === recipe.recipeId;
            const rescuesPriority = recipe.priorityFoodIds && recipe.priorityFoodIds.length > 0;
            const totalIngs = recipe.ingredients.length;
            const availableIngs = recipe.ingredients.filter((i) => i.missingQuantity <= 0).length;
            const matchPercent = totalIngs > 0 ? Math.round((availableIngs / totalIngs) * 100) : 0;

            return (
              <article key={recipe.recipeId} className="recipe-item-card">
                <div className="recipe-card-top">
                  <div className="recipe-title-group">
                    <h3 className="recipe-title">{recipe.title}</h3>
                    <div className="recipe-meta-tags">
                      <span className="meta-tag">{recipe.servings} porciones</span>
                      <span className={`match-badge ${matchPercent === 100 ? 'full' : 'partial'}`}>
                        {matchPercent}% ingredientes en casa
                      </span>
                    </div>
                  </div>
                  {rescuesPriority && (
                    <span className="priority-rescuer-badge">
                      <SparklesIcon size={12} />
                      <span>Salva prioritarios</span>
                    </span>
                  )}
                </div>

                <div className="recipe-ingredient-chips">
                  {recipe.ingredients.map((ing) => {
                    const ready = ing.missingQuantity <= 0;
                    return (
                      <span key={ing.ingredientId} className={`ing-chip ${ready ? 'ready' : 'needed'}`}>
                        {ready ? <CheckCircleIcon size={12} /> : <span className="dot-needed" />}
                        <span>{ing.name} ({ing.requiredQuantity} {ing.unit})</span>
                        {!ready && <small className="missing-text">Faltan {ing.missingQuantity}</small>}
                      </span>
                    );
                  })}
                </div>

                <button
                  className="btn-toggle-instructions"
                  onClick={() => setActiveRecipeId(isExpanded ? null : recipe.recipeId)}
                >
                  <span>{isExpanded ? 'Cerrar preparación' : 'Ver instrucciones'}</span>
                  <svg className={`chevron ${isExpanded ? 'open' : ''}`} width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <polyline points="6 9 12 15 18 9" />
                  </svg>
                </button>

                {isExpanded && (
                  <div className="recipe-expanded-steps">
                    <h4>Procedimiento</h4>
                    {renderInstructions(recipe.instructions)}
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
};
