import React from 'react';
import { DateStatus, DateType } from '../types/api';
import { ClockIcon, AlertCircleIcon, CheckCircleIcon } from './Icons';

interface Props {
  status?: DateStatus;
  dateType: DateType;
  daysRemaining?: number;
  labelDate?: string;
}

export const StatusBadge: React.FC<Props> = ({ status, dateType, daysRemaining, labelDate }) => {
  const typeTag = dateType === 'EXPIRATION' ? 'Caducidad' : 'Cons. preferente';

  // Si el backend no envió daysRemaining, lo calculamos exactamente con labelDate
  let diffDays = daysRemaining;
  if (diffDays === undefined && labelDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const [y, m, d] = labelDate.split('-').map(Number);
    const target = new Date(y, m - 1, d);
    diffDays = Math.round((target.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  let tone = 'tone-normal';
  let label = 'En tiempo';
  let Icon = CheckCircleIcon;

  if (diffDays !== undefined) {
    if (diffDays < 0) {
      tone = 'tone-expired';
      Icon = AlertCircleIcon;
      label = 'Vencido';
    } else if (diffDays === 0) {
      tone = 'tone-urgent';
      Icon = AlertCircleIcon;
      label = 'Vence hoy';
    } else if (diffDays === 1) {
      tone = 'tone-urgent';
      Icon = AlertCircleIcon;
      label = 'Vence mañana';
    } else if (diffDays <= 3) {
      tone = 'tone-urgent';
      Icon = AlertCircleIcon;
      label = `${diffDays} días restantes`;
    } else {
      tone = 'tone-normal';
      Icon = ClockIcon;
      label = `${diffDays} días restantes`;
    }
  } else {
    if (status === 'PRIORITY') {
      tone = 'tone-urgent';
      Icon = AlertCircleIcon;
      label = 'Prioritario';
    } else if (status === 'DATE_PASSED') {
      tone = 'tone-expired';
      Icon = AlertCircleIcon;
      label = 'Vencido';
    } else {
      label = 'Fresco';
    }
  }

  return (
    <div className="status-row">
      <span className={`status-pill ${tone}`}>
        <Icon size={13} />
        <span>{label}</span>
      </span>
      <span className="date-type-chip">{typeTag}</span>
    </div>
  );
};