// @Entity
// @Table(name = "chat_link")
// @Getter
// @Setter
// @NoArgsConstructor
// public class ChatLink {
//
//    @Embeddable
//    @NoArgsConstructor
//    public static class Id implements Serializable {
//        @Column(name = "chat_id")
//        @Getter
//        protected Long chatId;
//
//        @Column(name = "link_url")
//        @Getter
//        protected String linkUrl;
//
//        public Id(String linkUrl, Long chatId) {
//            this.linkUrl = linkUrl;
//            this.chatId = chatId;
//        }
//
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o != null && o instanceof Id) {
//                Id that = (Id) o;
//                return this.chatId.equals(that.chatId) && this.linkUrl.equals(that.linkUrl);
//            }
//            return false;
//        }
//
//        public int hashCode() {
//            int result = chatId == null ? 0 : chatId.hashCode();
//            result = 31 * result + (linkUrl == null ? 0 : linkUrl.hashCode());
//            return result;
//        }
//    }
//
//    @EmbeddedId
//    private Id id;
//
//    @Column(name = "business_id", insertable = false, updatable = false)
//    private Long BusinessId;
//
//    @ManyToOne
//    @JoinColumn(
//        name = "link_url",
//        insertable = false, updatable = false
//    )
//    private Link link;
//
//    @ManyToOne
//    @JoinColumn(
//        name = "chat_id",
//        insertable = false, updatable = false
//    )
//    private Chat chat;
//
//
//    @ElementCollection
//    @CollectionTable(
//        name = "chat_link_tags",
//        joinColumns = {
//            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
//            @JoinColumn(name = "link_url", referencedColumnName = "url")
//        }
//    )
//    @Column(name = "tag")
//    private Set<String> tags = new HashSet<>();
//
//    public ChatLink(@NotNull Link link, @NotNull Chat chat) {
//        this.link = link;
//        this.chat = chat;
//
//        this.id = new Id(link.getUrl(), chat.getId());
//
//        link.addChat(this);
//        chat.addLink(this);
//    }
//
//    public ChatLink(@NotNull Link link, @NotNull Chat chat, Set<String> tags) {
//        this(link, chat);
//
//        if (tags != null) {
//            this.tags = tags;
//        }
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof ChatLink)) return false;
//        ChatLink that = (ChatLink) o;
//        return Objects.equals(id, that.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id);
//    }
//
// }
