package net.okocraft.box.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 取引量、前のメニューへの帰還、そしてページ変更ボタンを追加したGUIの実装
 */
abstract class PagedGUI extends BaseGUI implements Clickable {

    private final int previousPageSlot;
    private final int nextPageSlot;

    private final Player player;

    /**
     * コンストラクタ
     *
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param guiTitle     GUIのタイトル
     */
    public PagedGUI(Player player, String guiTitle, int GUISize, int previousPageSlot, int nextPageSlot) {
        super(GUISize, guiTitle);

        this.previousPageSlot = previousPageSlot;
        this.nextPageSlot = nextPageSlot;

        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<>() {{
            put(previousPageSlot, layout.getPreviousPage());
            put(nextPageSlot, layout.getNextPage());
        }};
        putPageCommonItems(pageCommonItems);

        this.player = player;
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        if (event.getSlot() == nextPageSlot) {
            setPage(getPage() + 1);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.MASTER, 1F, 1F);
            return;
        } else if (event.getSlot() == previousPageSlot) {
            setPage(getPage() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.MASTER, 1F, 1F);
            return;
        }
    }

    protected ItemStack applyPagePlaceholder(ItemStack item, Map<String, String> placeholder) {
        placeholder.put("%page%", String.valueOf(getPage()));
        placeholder.put("%next-page%", String.valueOf(getPage() + 1));
        placeholder.put("%previous-page%", String.valueOf(getPage() - 1));
        placeholder.put("%max-page%", String.valueOf(getMaxPage()));
        return super.applyPlaceholder(item, placeholder);
    }

    @Override
    public void setPage(int page) {
        super.setPage(page);
        updatePageArrowIcon(true);
        updatePageArrowIcon(false);
    }
    
    private void updatePageArrowIcon(boolean isNextPageIcon) {
        int invSize = getInventory().getSize();
        int slot = isNextPageIcon ? nextPageSlot : previousPageSlot;
        if (invSize > slot) {
            ItemStack icon = getInventory().getItem(slot);
            if (icon != null && icon.getType() == Material.ARROW) {
                icon.setItemMeta(isNextPageIcon
                    ? layout.getNextPage().getItemMeta()
                    : layout.getPreviousPage().getItemMeta()
                );
                applyPagePlaceholder(icon, new HashMap<>());
                icon.setAmount(getPage() + (isNextPageIcon ? 1 : -1));
            }
        }
    }

    /**
     * このGUIを開いたプレイヤーを取得する。
     * 
     * @return このGUIを開いたプレイヤー
     */
    public Player getPlayer() {
        return player;
    }
}