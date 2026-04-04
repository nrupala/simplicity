import { useState, useEffect } from 'react';
import axios from 'axios';
import { Search, Shield, Database, Zap, User, Settings, Download, Upload } from 'lucide-react';

interface QueryResult {
  text: string;
  profile: any;
  knowledgeUsed: number;
  processingTime: number;
}

interface KnowledgeNode {
  id: string;
  label: string;
  type: string;
}

export default function App() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<QueryResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [knowledgeGraph, setKnowledgeGraph] = useState<KnowledgeNode[]>([]);
  const [activeTab, setActiveTab] = useState<'query' | 'knowledge' | 'sovereignty'>('query');

  const handleQuery = async () => {
    if (!query.trim()) return;

    setLoading(true);
    try {
      const response = await axios.post('http://localhost:3001/api/query', {
        query: query.trim(),
        userId: 'default-user' // In real app, get from auth
      });
      setResults(response.data);
    } catch (error) {
      console.error('Query failed:', error);
      setResults({
        text: 'Sorry, I encountered an error processing your query. Please try again.',
        profile: { tone: 'professional' },
        knowledgeUsed: 0,
        processingTime: 0
      });
    } finally {
      setLoading(false);
    }
  };

  const loadKnowledgeGraph = async () => {
    try {
      const response = await axios.get('http://localhost:3001/api/knowledge/graph/default-user');
      setKnowledgeGraph(response.data.nodes || []);
    } catch (error) {
      console.error('Failed to load knowledge graph:', error);
    }
  };

  const exportData = async () => {
    try {
      const response = await axios.get('http://localhost:3001/api/portability/export/default-user');
      const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'simplicity-data.json';
      a.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Export failed:', error);
    }
  };

  const importData = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const data = JSON.parse(e.target?.result as string);
        await axios.post('http://localhost:3001/api/portability/import/default-user', data);
        loadKnowledgeGraph();
        alert('Data imported successfully!');
      } catch (error) {
        console.error('Import failed:', error);
        alert('Import failed. Please check the file format.');
      }
    };
    reader.readAsText(file);
  };

  useEffect(() => {
    if (activeTab === 'knowledge') {
      loadKnowledgeGraph();
    }
  }, [activeTab]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-2">
              <Zap className="h-8 w-8 text-indigo-600" />
              <h1 className="text-2xl font-bold text-gray-900">SIMPLICITY</h1>
              <span className="text-sm text-gray-500">Your Sovereign AI Agent</span>
            </div>
            <div className="flex space-x-4">
              <button
                onClick={() => setActiveTab('query')}
                className={`px-4 py-2 rounded-md ${activeTab === 'query' ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100'}`}
              >
                <Search className="h-4 w-4 inline mr-2" />
                Query
              </button>
              <button
                onClick={() => setActiveTab('knowledge')}
                className={`px-4 py-2 rounded-md ${activeTab === 'knowledge' ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100'}`}
              >
                <Database className="h-4 w-4 inline mr-2" />
                Knowledge
              </button>
              <button
                onClick={() => setActiveTab('sovereignty')}
                className={`px-4 py-2 rounded-md ${activeTab === 'sovereignty' ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100'}`}
              >
                <Shield className="h-4 w-4 inline mr-2" />
                Sovereignty
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'query' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold mb-4">Ask SIMPLICITY</h2>
              <div className="flex space-x-4">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleQuery()}
                  placeholder="Enter your question..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                />
                <button
                  onClick={handleQuery}
                  disabled={loading}
                  className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {loading ? 'Thinking...' : 'Ask'}
                </button>
              </div>
            </div>

            {results && (
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold">Response</h3>
                  <div className="text-sm text-gray-500">
                    Knowledge used: {results.knowledgeUsed} • Time: {results.processingTime}ms
                  </div>
                </div>
                <div className="prose max-w-none">
                  <pre className="whitespace-pre-wrap">{results.text}</pre>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'knowledge' && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Knowledge Graph</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {knowledgeGraph.map((node) => (
                <div key={node.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="font-medium text-gray-900">{node.label}</div>
                  <div className="text-sm text-gray-500">{node.type}</div>
                </div>
              ))}
            </div>
            {knowledgeGraph.length === 0 && (
              <p className="text-gray-500 text-center py-8">No knowledge nodes yet. Ask some questions to build your knowledge graph!</p>
            )}
          </div>
        )}

        {activeTab === 'sovereignty' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold mb-4">Data Sovereignty</h2>
              <div className="space-y-4">
                <div className="flex items-center space-x-4">
                  <Shield className="h-6 w-6 text-green-600" />
                  <div>
                    <div className="font-medium">Your Data is Encrypted</div>
                    <div className="text-sm text-gray-500">All knowledge is stored locally with GPG encryption</div>
                  </div>
                </div>
                <div className="flex items-center space-x-4">
                  <User className="h-6 w-6 text-blue-600" />
                  <div>
                    <div className="font-medium">You Own Your Data</div>
                    <div className="text-sm text-gray-500">No cloud storage, complete local control</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold mb-4">Data Management</h2>
              <div className="flex space-x-4">
                <button
                  onClick={exportData}
                  className="flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Export Data
                </button>
                <label className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 cursor-pointer">
                  <Upload className="h-4 w-4 mr-2" />
                  Import Data
                  <input
                    type="file"
                    accept=".json"
                    onChange={importData}
                    className="hidden"
                  />
                </label>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
height: '100vh',
  backgroundColor: '#f7fafc',
    fontFamily: 'system-ui, sans-serif'
    }}>

  {/* LEFT COLUMN: VISUAL LAB */ }
  < div style = {{ display: 'flex', flexDirection: 'column', padding: '25px', gap: '20px', overflow: 'hidden' }}>

        <nav style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          {Object.values(EQUIPMENT_TYPES).map((eq) => (
            <button
              key={eq.id}
              onClick={() => { setActiveTab(eq.id); setShowHelp(false); }}
              style={{
                padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', border: 'none', fontWeight: 'bold',
                backgroundColor: activeTab === eq.id && !showHelp ? '#3182ce' : '#edf2f7',
                color: activeTab === eq.id && !showHelp ? '#fff' : '#4a5568'
              }}
            >
              {eq.label}
            </button>
          ))}
          <button
            onClick={() => setShowHelp(!showHelp)}
            style={{
              padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold',
              border: '2px solid #3182ce', backgroundColor: showHelp ? '#3182ce' : 'transparent',
              color: showHelp ? '#fff' : '#3182ce', marginLeft: 'auto'
            }}
          >
            {showHelp ? '✕ Close Help' : '📚 ANSI Docs'}
          </button>
        </nav>

        <div style={{
          flex: 1, backgroundColor: '#fff', borderRadius: '16px', border: '1px solid #e2e8f0',
          padding: '30px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)', overflowY: 'auto'
        }}>
          {showHelp ? (
            <HelpSection activeFaultCode={fault.type} />
          ) : (
            <div style={{ textAlign: 'center' }}>
              <h2 style={{ color: '#1a365d', marginBottom: '20px' }}>{config.label} Protection Analysis</h2>
              <EquipmentView type={activeTab} isTripped={isTripped} />

              {isTripped && (
                <div style={{ marginTop: '25px', padding: '20px', backgroundColor: '#fff5f5', border: '2px solid #feb2b2', borderRadius: '12px' }}>
                  <h3 style={{ margin: 0, color: '#c53030' }}>🚨 {fault.type} OPERATION</h3>
                  <div style={{ marginTop: '15px', padding: '10px', background: '#fff', borderRadius: '8px', border: '1px solid #feb2b2', textAlign: 'left' }}>
                    <div style={{ fontSize: '11px', color: '#718096', fontWeight: 'bold', marginBottom: '5px' }}>IEEE COORDINATION MATH:</div>
                    <code style={{ fontSize: '13px', color: '#2d3748', display: 'block', lineHeight: '1.6' }}>
                      {fault.type === '51' ? (
                        <>
                          Time = TD × [ (A / (M<sup>p</sup> - 1)) + B ] <br />
                          Time = 0.5 × [ (19.61 / ({(avgI / 1.5).toFixed(2)}<sup>2</sup> - 1)) + 0.491 ] = <strong>{result.time.toFixed(3)}s</strong>
                        </>
                      ) : (
                        <>Instantaneous Trip (Device 50) <br /> Time = 1 Cycle (0.016s) @ M &gt; 8.0</>
                      )}
                    </code>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div >

  {/* RIGHT COLUMN: DASHBOARD & CONTROLS */ }
  < aside style = {{
  backgroundColor: '#fff', borderLeft: '1px solid #e2e8f0', padding: '25px',
    display: 'flex', flexDirection: 'column', gap: '20px', overflowY: 'auto'
}}>


        <div style={{ backgroundColor: '#1a202c', padding: '20px', borderRadius: '12px', color: '#63b3ed' }}>
          <h4 style={{ color: '#a0aec0', fontSize: '11px', margin: '0 0 15px 0', textTransform: 'uppercase' }}>Telemetry (IEEE 242)</h4>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div>
              <div style={{ fontSize: '10px', color: '#718096' }}>LOAD CURRENT</div>
              <div style={{ fontSize: '18px', fontWeight: 'bold' }}>{avgI.toFixed(2)} pu</div>
            </div>
            <div>
              <div style={{ fontSize: '10px', color: '#718096' }}>BUS VOLTAGE</div>
              <div style={{ fontSize: '18px', fontWeight: 'bold' }}>{config.nominalV} kV</div>
            </div>
            <div style={{ gridColumn: 'span 2', paddingTop: '10px', borderTop: '1px solid #2d3748' }}>
              <div style={{ fontSize: '10px', color: '#718096' }}>ACTIVE POWER</div>
              <div style={{ fontSize: '22px', fontWeight: 'bold', color: isTripped ? '#f56565' : '#48bb78' }}>{MW} MW</div>
            </div>
            <div style={{ backgroundColor: '#1a202c', padding: '20px', borderRadius: '12px', color: '#63b3ed' }}>
              {/* ... telemetry content ... */}
            </div>
          </div>
        </div>
        <SequencePhasor i1={i1} i2={i2} i0={i0} />
        <ControlPanel
          availableRelays={config.relays}
          currentFault={fault}
          onUpdate={setFault}
        />

        <div style={{ marginTop: 'auto', fontSize: '11px', color: '#a0aec0', textAlign: 'center' }}>
          RelaySim v3.0 • coordination Study
        </div>
      </aside >
    </div >
  );
}
