// src/components/EquipmentStudy.tsx
import React from 'react';

interface EquipmentStudyProps {
  isTripped: boolean;
  activeFault?: {
    code: string;
    name: string;
    explanation: string;
  };
}

export const EquipmentStudy = ({ isTripped, activeFault }: EquipmentStudyProps) => {
  const isTransformerFault = ['87T', '24', '63'].includes(activeFault?.code ?? '');
  const equipmentLabel = isTransformerFault ? 'Transformer Core' : 'Induction Motor';
  const statusLabel = isTripped ? (isTransformerFault ? 'OFFLINE' : 'TRIPPED') : (isTransformerFault ? 'ENERGIZED' : 'RUNNING');
  const faultDescription = activeFault?.name ? activeFault.name.toLowerCase() : 'an equipment issue';

  return (
    <div style={{ padding: '20px', background: '#fff', borderRadius: '15px', border: '1px solid #e2e8f0' }}>
      <h2 style={{ color: '#1a365d', marginBottom: '20px' }}>🏭 Industrial Equipment Study</h2>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '30px' }}>
        <div style={{
          background: '#f8fafc',
          borderRadius: '12px',
          padding: '40px',
          textAlign: 'center',
          border: '2px solid #edf2f7',
          minHeight: '400px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <div style={{ fontSize: '50px' }}>{isTripped ? '💥' : '⚡'}</div>
          <p>{equipmentLabel} {statusLabel}</p>

          <style>{`
            @keyframes spin {
              from { transform: rotate(0deg); }
              to { transform: rotate(360deg); }
            }
          `}</style>
        </div>

        <div>
          <div style={{
            padding: '20px',
            background: isTripped ? '#fff5f5' : '#ebf8ff',
            borderRadius: '12px',
            border: `1px solid ${isTripped ? '#feb2b2' : '#bee3f8'}`
          }}>
            <h4 style={{ margin: '0 0 10px 0' }}>System Analysis</h4>
            {isTripped ? (
              <>
                <p style={{ fontSize: '14px', color: '#c53030' }}>
                  <strong>Trip Event:</strong> The relay detected a <strong>{activeFault?.code}</strong> condition.
                </p>
                <p style={{ fontSize: '13px', color: '#4a5568' }}>
                  The circuit breaker was commanded to open to protect the asset from permanent damage.
                </p>
              </>
            ) : (
              <p style={{ fontSize: '14px', color: '#2c5282' }}>
                System is operating within nominal limits. The equipment is drawing balanced current.
              </p>
            )}
          </div>

          <div style={{ marginTop: '20px', fontSize: '13px', color: '#718096' }}>
            <h5>Study Notes:</h5>
            <ul style={{ paddingLeft: '20px' }}>
              <li>ANSI {activeFault?.code || 'N/A'} protects against {faultDescription}.</li>
              <li>Coordination interval: 300ms.</li>
              <li>CT Ratio: 100:5</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
