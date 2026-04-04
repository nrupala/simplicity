interface TCCPlotterProps {
  activeFault?: {
    code: string;
    name: string;
  };
  settings?: {
    curveType: string;
    TD: number;
    pickup: number;
  };
}

export const TCCPlotter = ({ activeFault, settings }: TCCPlotterProps) => {
  const faultLabel = activeFault ? `${activeFault.name} (${activeFault.code})` : 'No active relay selected';
  const curveInfo = settings ? `${settings.curveType} curve at ${settings.pickup} pu pickup` : 'Curve settings unavailable';

  return (
    <div className="tcc-container" style={{ padding: '20px', borderRadius: '12px', backgroundColor: '#fff', border: '1px solid #e2e8f0' }}>
      <h4 style={{ marginTop: 0 }}>IEEE Coordination Plot (Log-Log)</h4>
      <p style={{ margin: '10px 0', color: '#4a5568', fontSize: '13px' }}>{faultLabel}</p>
      <p style={{ margin: 0, color: '#718096', fontSize: '12px' }}>{curveInfo}</p>
      <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#f8fafc', borderRadius: '10px' }}>
        {/* Placeholder for future plot rendering */}
        <p style={{ margin: 0, color: '#2d3748' }}>Plot area coming soon.</p>
      </div>
    </div>
  );
};
