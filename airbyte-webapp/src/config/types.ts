declare global {
  interface Window {
    TRACKING_STRATEGY?: string;
    BASE_URL?: string;
  }
}

export interface AirbyteWebappConfig {
  segment: { token?: string; enabled: boolean };
  baseUrl: string;
  fathomSiteId?: string;
  apiUrl: string;
  connectorBuilderApiUrl: string;
  version?: string;
  cloudApiUrl: string;
  cloudPublicApiUrl?: string;
  firebase: {
    apiKey?: string;
    authDomain?: string;
    authEmulatorHost?: string;
  };
  zendeskKey?: string;
  launchDarkly?: string;
  datadog: {
    applicationId?: string;
    clientToken?: string;
    site?: string;
    service?: string;
  };
}
