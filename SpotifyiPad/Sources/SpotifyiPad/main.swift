import SwiftUI
import WebKit

@main
struct SpotifyiPadApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @StateObject private var model = SpotifyWebViewModel()

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            SpotifyWebView(model: model)
                .ignoresSafeArea(edges: [.bottom, .leading, .trailing])

            if model.isLoading {
                VStack {
                    Spacer()
                    ProgressView(value: model.progress)
                        .progressViewStyle(.linear)
                        .tint(.green)
                        .padding(.horizontal, 24)
                        .padding(.bottom, 10)
                }
            }
        }
        .safeAreaInset(edge: .top) {
            HStack(spacing: 12) {
                Button { model.goBack() } label: {
                    Image(systemName: "chevron.left")
                }
                .disabled(!model.canGoBack)

                Button { model.goForward() } label: {
                    Image(systemName: "chevron.right")
                }
                .disabled(!model.canGoForward)

                Spacer()

                Button { model.reload() } label: {
                    Image(systemName: "arrow.clockwise")
                }

                Button { model.openInSafari() } label: {
                    Image(systemName: "safari")
                }
            }
            .font(.headline)
            .foregroundStyle(.white)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(.black.opacity(0.92))
        }
        .statusBarHidden(true)
        .onAppear {
            model.loadIfNeeded()
        }
    }
}

final class SpotifyWebViewModel: NSObject, ObservableObject, WKNavigationDelegate {
    @Published var isLoading = false
    @Published var progress = 0.0
    @Published var canGoBack = false
    @Published var canGoForward = false

    let webView: WKWebView
    private var progressObservation: NSKeyValueObservation?
    private var stateObservation: NSKeyValueObservation?
    private var didLoad = false

    override init() {
        let config = WKWebViewConfiguration()
        config.websiteDataStore = .default()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []

        self.webView = WKWebView(frame: .zero, configuration: config)
        super.init()

        webView.navigationDelegate = self
        webView.allowsBackForwardNavigationGestures = true

        progressObservation = webView.observe(\ .estimatedProgress, options: [.initial, .new]) { [weak self] webView, _ in
            DispatchQueue.main.async {
                self?.progress = webView.estimatedProgress
            }
        }

        stateObservation = webView.observe(\ .canGoBack, options: [.initial, .new]) { [weak self] webView, _ in
            DispatchQueue.main.async {
                self?.canGoBack = webView.canGoBack
                self?.canGoForward = webView.canGoForward
            }
        }
    }

    func loadIfNeeded() {
        guard !didLoad else { return }
        didLoad = true
        guard let url = URL(string: "https://open.spotify.com/") else { return }
        webView.load(URLRequest(url: url, cachePolicy: .useProtocolCachePolicy))
    }

    func goBack() {
        guard webView.canGoBack else { return }
        webView.goBack()
    }

    func goForward() {
        guard webView.canGoForward else { return }
        webView.goForward()
    }

    func reload() {
        webView.reload()
    }

    func openInSafari() {
        guard let url = webView.url ?? URL(string: "https://open.spotify.com/") else { return }
        UIApplication.shared.open(url)
    }

    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        isLoading = true
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        isLoading = false
        canGoBack = webView.canGoBack
        canGoForward = webView.canGoForward
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        isLoading = false
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        isLoading = false
    }
}

struct SpotifyWebView: UIViewRepresentable {
    @ObservedObject var model: SpotifyWebViewModel

    func makeUIView(context: Context) -> WKWebView {
        model.webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {}
}
