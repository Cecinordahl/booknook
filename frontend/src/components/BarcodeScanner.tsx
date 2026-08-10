import { useEffect, useRef, useState } from "react";
import { BrowserMultiFormatReader } from "@zxing/browser";
import type { IScannerControls } from "@zxing/browser";

interface BarcodeScannerProps {
  onScan: (isbn: string) => void;
}

/** Scans a book's barcode (EAN-13, which is what ISBNs are encoded as) via the device camera. */
export function BarcodeScanner({ onScan }: BarcodeScannerProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const reader = new BrowserMultiFormatReader();
    let controls: IScannerControls | undefined;
    let cancelled = false;

    reader
      .decodeFromVideoDevice(undefined, videoRef.current ?? undefined, (result) => {
        if (result && !cancelled) {
          onScan(result.getText());
        }
      })
      .then((c) => {
        controls = c;
      })
      .catch((e) => {
        setError(e instanceof Error ? e.message : "Could not access the camera.");
      });

    return () => {
      cancelled = true;
      controls?.stop();
    };
  }, [onScan]);

  if (error) {
    return <p className="error-text">{error} Make sure you've granted camera permission.</p>;
  }

  return (
    <video ref={videoRef} style={{ width: "100%", maxWidth: 400, borderRadius: "var(--radius)" }} muted playsInline />
  );
}
