// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SpotifyiPad",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .iOSApplication(
            name: "Spotify iPad",
            targets: ["SpotifyiPad"],
            bundleIdentifier: "com.benjighg.spotifyipad",
            teamIdentifier: "",
            displayVersion: "1.0",
            bundleVersion: "1",
            appIcon: .placeholder(iconName: "AppIcon"),
            accentColor: .presetColor(.green),
            supportedDeviceFamilies: [.pad],
            additionalInfoPlistContentFilePath: "Info.plist"
        )
    ],
    targets: [
        .executableTarget(
            name: "SpotifyiPad",
            path: "Sources/SpotifyiPad"
        )
    ]
)
