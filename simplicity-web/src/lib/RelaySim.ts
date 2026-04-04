// src/lib/RelaySim.ts
export interface RelayFault {
  type: string;
  Iabc: number[];
  Iang: number[];
  V?: number;
  f?: number;
}

export class RelaySim {
  run(fault: RelayFault) {
    const { type, Iabc, Iang, V = 1.0, f = 60 } = fault;
    const maxI = Math.max(...Iabc);
    const pickup = 1.5; // Standard 1.5pu Pickup
    const TD = 0.5;    // Time Dial

    // --- 1. Symmetrical Components (Calculated for 46 & 50G) ---
    const i0 = Math.abs(Iabc[0] + Iabc[1] + Iabc[2]) / 3;
    const i2 = (maxI - Math.min(...Iabc)) / 2; // Simplified I2 approximation

    switch (type) {
      case '50': {
        return { trip: maxI > 8.0, time: 0.016 };
      }
      case '51': {
        const M = maxI / pickup;
        if (M <= 1) {
          return { trip: false, time: Infinity };
        }
        // IEEE Very Inverse: t = TD * (19.61 / (M^2 - 1) + 0.491)
        return { trip: true, time: TD * (19.61 / (Math.pow(M, 2) - 1) + 0.491) };
      }
      case '50G': {
        return { trip: i0 > 0.2, time: 0.016 };
      }
      case '46': {
        return { trip: i2 / maxI > 0.15, time: 0.5 };
      }
      case '49': {
        return { trip: maxI > 1.1, time: 10.0 };
      }
      case '87T':
      case '87B': {
        return { trip: maxI > 2.0, time: 0.02 };
      }
      case '24': {
        return { trip: (V / (f / 60)) > 1.1, time: 2.0 };
      }
      case '21': {
        const Z = V / maxI;
        return { trip: Z < 0.5, time: 0.05 };
      }
      case '67': {
        return { trip: maxI > pickup && Iang[0] > 90, time: 0.1 };
      }
      default: {
        return { trip: false, time: 0 };
      }
    }
  }
}
